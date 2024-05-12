/**
 *@author Nikolaus Knop
 */

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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.jsonArray
import reaktive.list.MutableReactiveList
import reaktive.list.ReactiveList
import reaktive.list.binding.values
import reaktive.list.reactiveList
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding
import kotlin.reflect.full.isSubclassOf

/**
 * An editor for multiple child editors of type [E] whose result type is [R]
 */
@ProvideFeature
abstract class ListEditor<R, E : Editor<R>>(
    context: Context,
    private val _editors: MutableReactiveList<E>
) : AbstractEditor<List<R>, ListEditorView>(context) {
    constructor(context: Context) : this(context, reactiveList())

    private val editorClass = javaClass.getMethod("createEditor").returnType.kotlin

    private var mayBeEmpty = true

    private fun mayRemove() = mayBeEmpty || editors.now.size > 1

    private val undo = context[UndoManager]

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

    override val result: ReactiveValue<List<R>> = binding(results) { results.now.toList() }

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
        context.withoutUndo {
            if (!mayBeEmpty && size == 0) return false
            val old = editors.now.size
            when {
                old > size -> repeat(size - old) { i ->
                    doAddAt(old + i, tryCreateEditor() ?: error("createEditor() returned null"))
                }
                old < size -> for (i in size downTo old) {
                    removeAt(i)
                }
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
            val snapshots = editors.now.map { it.snapshot(recordClass = true) }
            val edit = ClearEdit(virtualize(), snapshots)
            undo.record(edit)
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
    fun pasteMany(idx: Int, editors: List<Snapshot<*>>, undoable: Boolean = true) {
        editors as List<Snapshot<E>>
        if (undoable) {
            val edit = PasteManyEdit(virtualize(), idx, editors)
            undo.record(edit)
        }
        if (editors.any { !editorClass.isInstance(it) }) return
        for ((i, e) in editors.withIndex()) {
            doAddAt(idx + i, e.reconstructEditor(childContext()))
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

    override fun paste(snapshot: Snapshot<out Editor<*>>): Boolean {
        if (snapshot !is Snap<*>) return false
        for (child in snapshot.snapshots) {
            if (!child.originalClass().isSubclassOf(editorClass)) {
                return false
            }
        }
        @Suppress("UNCHECKED_CAST")
        snapshot as Snapshot<Any>
        snapshot.reconstructObject(this)
        return true
    }

    override fun createSnapshot(): Snapshot<*> = Snap<E>()

    override fun supportsCopyPaste(): Boolean = editors.now.all { e -> e.supportsCopyPaste() }

    /**
     * Add a new editor at the given index using [createEditor] to create a new editor
     */
    fun addAt(index: Int): E? {
        val editor = tryCreateEditor() ?: return null
        if (undo.isActive) {
            val edit = AddEdit(virtualize(), index, editor.snapshot(recordClass = true))
            undo.record(edit)
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
    fun addAt(index: Int, editor: E): E {
        if (undo.isActive) {
            val edit = AddEdit(virtualize(), index, editor.snapshot(recordClass = true))
            undo.record(edit)
        }
        val e = editor.moveTo(childContext())
        doAddAt(index, e)
        return e
    }

    /**
     * Add the new [editor] at the end of the editor list
     * @return the moved editor
     */
    fun addLast(editor: E): E {
        return addAt(editors.now.size, editor)
    }

    /**
     * Create a new editor with [createEditor] and insert it as the last editor.
     */
    fun addLast(): E? {
        val e = tryCreateEditor() ?: return null
        return addLast(e)
    }

    /**
     * Remove the editor at the specified [index]
     */
    fun removeAt(index: Int) {
        if (!mayRemove()) return
        val old = _editors.now.removeAt(index)
        updateIndicesFrom(index)
        views { removed(index) }
        if (emptyNow()) views { empty() }
        context.executeSafely("removing editor", Unit) { editorRemoved(old, index) }
        if (undo.isActive) {
            val edit = RemoveEdit(virtualize(), index, old.snapshot(recordClass = true))
            undo.record(edit)
        }
    }

    @ProvideCommand(name = "Remove editor", shortName = "remove")
    private fun doRemove(index: Int) {
        removeAt(index)
    }

    /**
     * Remove the given [editor] from this [ListEditor].
     */
    fun remove(editor: E) {
        val idx = editors.now.indexOf(editor)
        removeAt(idx)
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
    @Suppress("DEPRECATION")
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

    @Suppress("DEPRECATION")
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
        private val added: Snapshot<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            val e = editor.get()
            e.addAt(index, added.reconstructEditor(e.context))
        }

        override fun doUndo() {
            editor.get().removeAt(index)
        }

        override val actionDescription: String
            get() = "Add element"
    }

    private class RemoveEdit<E : Editor<*>>(
        private val editor: VirtualEditor<ListEditor<*, E>>,
        private val index: Int,
        private val removed: Snapshot<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.get().removeAt(index)
        }

        override fun doUndo() {
            val e = editor.get()
            e.addAt(index, removed.reconstructEditor(e.context))
        }

        override val actionDescription: String
            get() = "Remove element"
    }

    private class ClearEdit<E : Editor<*>>(
        private val editor: VirtualEditor<ListEditor<*, E>>,
        private val removed: List<Snapshot<E>>
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
        private val pasted: List<Snapshot<E>>
    ) : AbstractEdit() {
        override fun doRedo() {
            val e = editor.get()
            e.pasteMany(index, pasted, undoable = false)
        }

        override fun doUndo() {
            val e = editor.get()
            repeat(pasted.size) {
                e.removeAt(index)
            }
        }

        override val actionDescription: String
            get() = "Paste many"
    }

    private class Snap<E : Editor<*>> : Snapshot<ListEditor<*, E>>() {
        lateinit var snapshots: List<Snapshot<E>>
            private set

        override fun doRecord(original: ListEditor<*, E>) {
            snapshots = original.editors.now.map { it.snapshot(recordClass = true) }
        }

        override fun reconstructObject(original: ListEditor<*, E>) {
            val editors = snapshots.map { it.reconstructEditor(original.childContext()) }
            original.setEditors(editors)
        }

        override fun encode(builder: JsonObjectBuilder) {
            val editors = snapshots.map { it.encodeToJson() }
            builder.put("editors", JsonArray(editors))
        }

        override fun decode(element: JsonObject) {
            val editors = element.getValue("editors").jsonArray
            snapshots = editors.map { decodeFromJson<E>(it) }
        }
    }
}