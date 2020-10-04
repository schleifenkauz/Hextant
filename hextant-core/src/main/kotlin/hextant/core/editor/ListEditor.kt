/**
 *@author Nikolaus Knop
 */

@file:Suppress("DEPRECATION")

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.command.meta.ProvideCommand
import hextant.context.*
import hextant.context.ClipboardContent.MultipleEditors
import hextant.core.Editor
import hextant.core.view.ListEditorView
import hextant.serial.*
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kserial.Serializable
import reaktive.dependencies
import reaktive.list.*
import reaktive.list.binding.values
import reaktive.value.binding.binding
import validated.*
import validated.Validated.InvalidComponent
import validated.Validated.Valid
import validated.reaktive.ReactiveValidated

/**
 * An editor for multiple child editors of type [E] whose result type is [R]
 */
@ProvideFeature
abstract class ListEditor<R, E : Editor<R>>(
    context: Context,
    private val _editors: MutableReactiveList<E>
) : AbstractEditor<List<R>, ListEditorView>(context), Serializable {
    constructor(context: Context) : this(context, reactiveList())

    private var mayBeEmpty = true

    private fun mayRemove() = mayBeEmpty || editors.now.size > 1

    private val undo = context[UndoManager]

    private val constructor by lazy { javaClass.getConstructor(Context::class.java) }

    private val editorClass by lazy { getTypeArgument(ListEditor::class, 1) }

    /**
     * All child editors of this [ListEditor]
     */
    val editors: ReactiveList<E> get() = _editors

    /**
     * All results of the child editors, the results always stay valid and fire changes when
     * * a new editor is added
     * * an editor is removed
     * * the result of a child editor changes
     */
    val results = editors.map { it.result }.values()

    /**
     * The [result] only contains an [Valid] value, if all child results are [Valid]. Otherwise it contains a [InvalidComponent]
     */
    override val result: ReactiveValidated<List<R>> = binding<Validated<List<R>>>(dependencies(results)) {
        results.now.takeIf { it.all { r -> r.isValid } }
            .validated { invalidComponent() }
            .map { results ->
                results.map { res -> res.force() }
            }
    }

    /**
     * Create a new Editor for results of type [E], or null if no new editor should be created
     */
    protected abstract fun createEditor(): E?

    private fun tryCreateEditor(): E? = context.executeSafely("create editor", null) { createEditor() }

    /**
     * Return the [Context] used for children of this [ListEditor].
     * The default implementation simply returns the [context] of this editor.
     */
    protected open fun childContext(): Context = context

    /**
     * Clears the children and then adds all the given new children.
     */
    fun setEditors(editors: List<E>): Boolean {
        if (!mayBeEmpty && editors.isEmpty()) return false
        doClear()
        for ((i, e) in editors.withIndex()) {
            doAddAt(i, e.copyFor(childContext()))
        }
        return true
    }

    /**
     * Adds or removes children such that this list editor has exactly [size] children.
     */
    fun resize(size: Int): Boolean {
        if (!mayBeEmpty && size == 0) return false
        val old = editors.now.size
        when {
            old < size -> repeat(size - old) { i ->
                doAddAt(old + i, tryCreateEditor() ?: error("createEditor() returned null"))
            }
            old > size -> for (i in size downTo old) {
                removeAt(i, undoable = false)
            }
        }
        return true
    }

    /**
     * Removes all editors. The method has no effect if [ensureNotEmpty] has been called before.
     */
    fun clear(undoable: Boolean = true) {
        if (!mayBeEmpty) return
        if (undoable) {
            val edit = ClearEdit(virtualize(), editors.now.map { it.snapshot() })
            undo.push(edit)
        }
        doClear()
    }

    private fun doClear() {
        for (e in editors.now) context.executeSafely("clearing editors", Unit) { editorRemoved(e, 0) }
        _editors.now.clear()
        views { empty() }
    }

    /**
     * Insert all the given [editors] at the specified [idx] into this [ListEditor].
     */
    @Suppress("UNCHECKED_CAST")
    fun pasteMany(idx: Int, editors: List<EditorSnapshot<*>>, undoable: Boolean = true) {
        editors as List<EditorSnapshot<E>>
        if (undoable) {
            val edit = PasteManyEdit(virtualize(), idx, editors)
            undo.push(edit)
        }
        if (editors.any { !editorClass.isInstance(it) }) return
        for ((i, e) in editors.withIndex()) {
            doAddAt(idx + i, e.reconstruct(childContext()))
        }
    }

    /**
     * Inserts all the editors currently copied with at the specified [idx] into this [ListEditor].
     */
    fun pasteManyFromClipboard(idx: Int) {
        val content = context[Clipboard].get()
        if (content !is MultipleEditors) return
        pasteMany(idx, content.snapshots)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is IndexAccessor) throw InvalidAccessorException(accessor)
        if (accessor.index >= editors.now.size) throw InvalidAccessorException(accessor)
        return editors.now[accessor.index]
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    override fun supportsCopyPaste(): Boolean = editors.now.all { e -> e.supportsCopyPaste() }

    /**
     * Add a new editor at the given index using [createEditor] to create a new editor
     */
    fun addAt(index: Int, undoable: Boolean = true): E? {
        val editor = tryCreateEditor() ?: return null
        if (undoable) {
            val edit = AddEdit(virtualize(), index, editor.snapshot())
            undo.push(edit)
        }
        doAddAt(index, editor)
        return editor
    }

    @ProvideCommand(name = "Add editor", shortName = "add")
    private fun doAddAt(index: Int) {
        addAt(index)
    }

    /**
     * Add the specified [editor] at the specified [index]
     * @return the moved editor
     */
    fun addAt(index: Int, editor: E, undoable: Boolean = true): E {
        if (undoable) {
            val edit = AddEdit(virtualize(), index, editor.snapshot())
            undo.push(edit)
        }
        val e = editor.moveTo(childContext())
        doAddAt(index, e)
        return e
    }

    /**
     * Add the new [editor] at the end of the editor list
     * @return the moved editor
     */
    fun addLast(editor: E, undoable: Boolean = true): E {
        return addAt(editors.now.size, editor, undoable)
    }

    /**
     * Create a new editor with [createEditor] and insert it as the last editor.
     */
    fun addLast(undoable: Boolean = true): E? {
        val e = tryCreateEditor() ?: return null
        return addLast(e, undoable)
    }

    /**
     * Remove the editor at the specified [index]
     */
    fun removeAt(index: Int, undoable: Boolean = true) {
        if (!mayRemove()) return
        val old = _editors.now.removeAt(index)
        updateIndicesFrom(index)
        views { removed(index) }
        if (emptyNow()) views { empty() }
        context.executeSafely("removing editor", Unit) { editorRemoved(old, index) }
        if (undoable) {
            val edit = RemoveEdit(virtualize(), index, old.snapshot())
            undo.push(edit)
        }
    }

    @ProvideCommand(name = "Remove editor", shortName = "remove")
    private fun doRemove(index: Int) {
        removeAt(index)
    }

    /**
     * Remove the given [editor] from this [ListEditor].
     */
    fun remove(editor: E, undoable: Boolean = true) {
        val idx = editors.now.indexOf(editor)
        removeAt(idx, undoable)
    }

    /**
     * Ensures that this list of editors is not empty now and will never be.
     * Repeated calls have no effect.
     */
    fun ensureNotEmpty() {
        if (!mayBeEmpty) return //already called
        mayBeEmpty = false
        if (editors.now.isEmpty()) {
            val e = tryCreateEditor() ?: error("createEditor() returned null")
            doAddAt(0, e)
        }
    }

    /**
     * Is called whenever a new editor is added to the list of editors forming this [ListEditor]
     */
    protected open fun editorAdded(editor: E, index: Int) {}

    /**
     * Is called whenever an editor removed from the list of editors forming this [ListEditor]
     */
    protected open fun editorRemoved(editor: E, index: Int) {}

    /**
     * Adds the given [editor] at the specified [index].
     */
    private fun doAddAt(index: Int, editor: E) {
        val emptyBefore = emptyNow()
        addChild(editor)
        editor.setAccessor(IndexAccessor(index))
        _editors.now.add(index, editor)
        updateIndicesFrom(index + 1)
        views {
            if (emptyBefore) notEmpty()
            added(editor, index)
        }
        context.executeSafely("adding editor", Unit) { editorAdded(editor, index) }
    }

    private fun updateIndicesFrom(index: Int) {
        for (i in index until editors.now.size) {
            editors.now[i].setAccessor(IndexAccessor(index))
        }
    }

    override fun viewAdded(view: ListEditorView) {
        if (emptyNow()) view.empty()
        else editors.now.forEachIndexed { i, editor ->
            view.added(editor, i)
        }
    }

    private fun emptyNow(): Boolean = editors.now.isEmpty()

    private class AddEdit<E : Editor<*>>(
        private val editor: VirtualEditor<ListEditor<*, E>>,
        private val index: Int,
        private val added: EditorSnapshot<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            val e = editor.get()
            e.addAt(index, added.reconstruct(e.context), undoable = false)
        }

        override fun doUndo() {
            editor.get().removeAt(index, undoable = false)
        }

        override val actionDescription: String
            get() = "Add element"
    }

    private class RemoveEdit<E : Editor<*>>(
        private val editor: VirtualEditor<ListEditor<*, E>>,
        private val index: Int,
        private val removed: EditorSnapshot<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.get().removeAt(index, undoable = false)
        }

        override fun doUndo() {
            val e = editor.get()
            e.addAt(index, removed.reconstruct(e.context), undoable = false)
        }

        override val actionDescription: String
            get() = "Remove element"
    }

    private class ClearEdit<E : Editor<*>>(
        private val editor: VirtualEditor<ListEditor<*, E>>,
        private val removed: List<EditorSnapshot<E>>
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.get().clear()
        }

        override fun doUndo() {
            val e = editor.get()
            e.pasteMany(0, removed, undoable = false)
        }

        override val actionDescription: String
            get() = "Clear"
    }

    private class PasteManyEdit<E : Editor<*>>(
        private val editor: VirtualEditor<ListEditor<*, E>>,
        private val index: Int,
        private val pasted: List<EditorSnapshot<E>>
    ) : AbstractEdit() {
        override fun doRedo() {
            val e = editor.get()
            e.pasteMany(index, pasted, undoable = false)
        }

        override fun doUndo() {
            val e = editor.get()
            repeat(pasted.size) {
                e.removeAt(index, undoable = false)
            }
        }

        override val actionDescription: String
            get() = "Paste many"
    }

    private class Snapshot<E : Editor<*>>(original: ListEditor<*, E>) :
        EditorSnapshot<ListEditor<*, E>>(original) {
        private val snapshots = original.editors.now.map { it.snapshot() }

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: ListEditor<*, E>) {
            val editors = snapshots.map { it.reconstruct(editor.childContext()) }
            editor.setEditors(editors)
        }
    }

    companion object {
        /**
         * Create a [ListEditor] which overrides [createEditor] such that it returns an editor for type results of [R]
         * using the specified [context]
         */
        @OptIn(ExperimentalStdlibApi::class)
        inline fun <reified R : Any> forType(context: Context): ListEditor<R, Editor<R>> =
            object : ListEditor<R, Editor<R>>(context) {
                override fun createEditor(): Editor<R> = context.createEditor<R>()
            }
    }
}