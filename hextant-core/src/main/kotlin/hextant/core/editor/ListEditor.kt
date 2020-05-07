/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import hextant.bundle.CoreProperties
import hextant.command.meta.ProvideCommand
import hextant.core.view.ListEditorView
import hextant.serial.*
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kserial.*
import reaktive.dependencies
import reaktive.list.*
import reaktive.list.binding.values
import reaktive.value.binding.binding

/**
 * An editor for multiple child editors of type [E] whose result type is [R]
 */
abstract class ListEditor<R : Any, E : Editor<R>>(
    context: Context,
    private val _editors: MutableReactiveList<E>
) : AbstractEditor<List<R>, ListEditorView>(context), Serializable {
    constructor(context: Context) : this(context, reactiveList())

    private var mayBeEmpty = false

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
     * The [result] only contains an [Ok] value, if all child results are [Ok]. Otherwise it contains a [ChildErr]
     */
    override val result: EditorResult<List<R>> = binding<CompileResult<List<R>>>(dependencies(results)) {
        results.now.takeIf { it.all { r -> r.isOk } }
            .okOrChildErr()
            .map { results ->
                results.map { res -> res.force() }
            }
    }

    /**
     * Create a new Editor for results of type [E], or null if no new editor should be created
     */
    protected abstract fun createEditor(): E?

    /**
     * Return the [Context] used for children of this [ListEditor].
     * The default implementation simply returns the [context] of this editor.
     */
    protected open fun childContext(): Context = context

    @Suppress("UNCHECKED_CAST")
    override fun paste(editor: Editor<*>): Boolean {
        if (editor !is ListEditor<*, *>) return false
        if (editor.editorClass != this.editorClass) return false
        if (!setEditors(editor.editors.now as List<E>)) return false
        return true
    }

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
     * Removes all editors
     */
    fun clear() {
        if (!mayBeEmpty) return
        val edit = ClearEdit(editors.now)
        doClear()
        undo.push(edit)
    }

    private fun doClear() {
        for (e in editors.now) editorRemoved(e, 0)
        _editors.now.clear()
        views { empty() }
    }

    /**
     * Insert all the given [editors] at the specified [idx] into this [ListEditor].
     */
    @Suppress("UNCHECKED_CAST")
    fun pasteMany(idx: Int, editors: List<*>) {
        if (editors.any { !editorClass.isInstance(it) }) return
        for ((i, e) in editors.withIndex()) {
            e as E
            doAddAt(idx + i, e.copyFor(childContext()))
        }
    }

    /**
     * Inserts all the editors currently copied with at the specified [idx] into this [ListEditor].
     */
    fun pasteManyFromClipboard(idx: Int) {
        val content = context[CoreProperties.clipboard]
        if (content !is List<*>) return
        pasteMany(idx, content)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is IndexAccessor) throw InvalidAccessorException(accessor)
        if (accessor.index >= editors.now.size) throw InvalidAccessorException(accessor)
        return editors.now[accessor.index]
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeInt(editors.now.size)
        for (e in editors.now) {
            output.writeString(e::class.java.name)
            output.writeUntyped(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(input: Input, context: SerialContext) {
        val size = input.readInt()
        for (idx in 0 until size) {
            val name = input.readString()
            val cls = Class.forName(name).kotlin
            val ed = cls.getSimpleEditorConstructor().invoke(childContext())
            doAddAt(idx, ed as E, notify = false)
            input.readInplace(ed)
        }
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    /**
     * Add a new editor at the given index using [createEditor] to create a new editor
     */
    @ProvideCommand(name = "Add editor", shortName = "add")
    fun addAt(index: Int): E? {
        val editor = createEditor() ?: return null
        val edit = AddEdit(index, editor)
        doAddAt(index, editor)
        undo.push(edit)
        return editor
    }

    /**
     * Add the specified [editor] at the specified [index]
     * @return the moved editor
     */
    fun addAt(index: Int, editor: E): E {
        val edit = AddEdit(index, editor)
        val e = editor.moveTo(childContext())
        doAddAt(index, e)
        undo.push(edit)
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
        val e = createEditor() ?: return null
        return addLast(e)
    }

    /**
     * Remove the editor at the specified [index]
     */
    @ProvideCommand(name = "Remove editor", shortName = "remove")
    open fun removeAt(index: Int) {
        val editor = editors.now[index]
        val edit = RemoveEdit(index, editor)
        if (doRemoveAt(index)) undo.push(edit)
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
     */
    fun ensureNotEmpty() {
        mayBeEmpty = false
        if (editors.now.isEmpty()) {
            val e = createEditor() ?: error("createEditor() returned false")
            doAddAt(0, e, notify = false)
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
     * Adds the given [editor] at the specified [index]. If [notify] is `false` [editorAdded] is not called.
     */
    protected fun doAddAt(index: Int, editor: E, notify: Boolean = true) {
        val emptyBefore = emptyNow()
        _editors.now.add(index, editor)
        addChild(editor)
        updateIndicesFrom(index)
        views {
            if (emptyBefore) notEmpty()
            added(editor, index)
        }
        if (notify) editorAdded(editor, index)
    }

    private fun updateIndicesFrom(index: Int) {
        for (i in index until editors.now.size) {
            @Suppress("DEPRECATION")
            editors.now[i].initAccessor(IndexAccessor(index))
        }
    }

    private fun doRemoveAt(index: Int): Boolean {
        if (!mayRemove()) return false
        val old = _editors.now.removeAt(index)
        updateIndicesFrom(index)
        views { removed(index) }
        if (emptyNow()) views { empty() }
        editorRemoved(old, index)
        return true
    }

    override fun viewAdded(view: ListEditorView) {
        if (emptyNow()) view.empty()
        else editors.now.forEachIndexed { i, editor ->
            view.added(editor, i)
        }
    }

    private fun emptyNow(): Boolean = editors.now.isEmpty()

    private inner class AddEdit(private val index: Int, private val editor: E) : AbstractEdit() {
        override fun doRedo() {
            doAddAt(index, editor)
        }

        override fun doUndo() {
            doRemoveAt(index)
        }

        override val actionDescription: String
            get() = "Add element"
    }

    private inner class RemoveEdit(private val index: Int, private val editor: E) : AbstractEdit() {
        override fun doRedo() {
            doRemoveAt(index)
        }

        override fun doUndo() {
            doAddAt(index, editor)
        }

        override val actionDescription: String
            get() = "Remove element"
    }

    private inner class ClearEdit(private val editors: List<E>) : AbstractEdit() {
        override fun doRedo() {
            clear()
        }

        override fun doUndo() {
            for (e in editors) addLast(e)
        }

        override val actionDescription: String
            get() = "Clear"
    }

    private class Snapshot<E : Editor<*>>(original: ListEditor<*, E>) :
        EditorSnapshot<ListEditor<*, E>>(original) {
        private val snapshots = original.editors.now.map { it.createSnapshot() }

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: ListEditor<*, E>) {
            val editors = snapshots.map { it.reconstruct(editor.childContext()) }
            editor.setEditors(editors as List<E>)
        }
    }

    companion object {
        /**
         * Create a [ListEditor] which overrides [createEditor] such that it returns an editor for type results of [R]
         * using the specified [context]
         */
        inline fun <reified R : Any> forType(context: Context): ListEditor<R, Editor<R>> =
            object : ListEditor<R, Editor<R>>(context) {
                override fun createEditor(): Editor<R> = context.createEditor(R::class)
            }
    }
}