/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.command.meta.ProvideCommand
import hextant.core.view.ListEditorView
import hextant.serial.*
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kserial.*
import reaktive.dependencies
import reaktive.list.ReactiveList
import reaktive.list.binding.values
import reaktive.list.reactiveList
import reaktive.value.binding.binding

/**
 * An editor for multiple child editors of type [E] whose result type is [R]
 */
abstract class ListEditor<R : Any, E : Editor<R>>(
    context: Context
) : AbstractEditor<List<R>, ListEditorView>(context), Serializable {
    private val undo = context[UndoManager]

    private val _editors = reactiveList<E>()

    private val constructor by lazy { javaClass.getConstructor(Context::class.java) }

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
     * Create a new Editor for results of type [E]
     */
    protected abstract fun createEditor(): E


    override fun copyForImpl(context: Context): Editor<List<R>> {
        val copy = constructor.newInstance(context)
        for ((i, e) in editors.now.withIndex()) {
            addAt(i, e)
        }
        return copy
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is IndexAccessor) throw InvalidAccessorException(accessor)
        if (accessor.index >= editors.now.size) throw InvalidAccessorException(accessor)
        return editors.now[accessor.index]
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeInt(editors.now.size)
        for (e in editors.now) {
            output.writeObject(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(input: Input, context: SerialContext) {
        val size = input.readInt()
        for (idx in 0 until size) {
            doAddAt(idx, input.readObject() as E, notify = false)
        }
    }

    /**
     * Add a new editor at the given index using [createEditor] to create a new editor
     */
    @ProvideCommand(name = "Add editor", shortName = "add")
    fun addAt(index: Int): E {
        val editor = createEditor()
        addAt(index, editor)
        return editor
    }

    /**
     * Add the specified [editor] at the specified [index]
     */
    fun addAt(index: Int, editor: E) {
        val edit = AddEdit(index, editor)
        doAddAt(index, editor)
        undo.push(edit)
        editorAdded(editor, index)
    }

    /**
     * Add a new editor at the end of the editor list
     */
    fun addLast(editor: E = createEditor()): E {
        addAt(editors.now.size, editor)
        return editor
    }

    /**
     * Add a new editor in front of the editor list
     */
    fun addFirst(editor: E = createEditor()): E {
        addAt(0, editor)
        return editor
    }

    /**
     * Remove the editor at the specified [index]
     */
    @ProvideCommand(name = "Remove editor", shortName = "remove")
    fun removeAt(index: Int) {
        val editor = editors.now[index]
        val edit = RemoveEdit(index, editor)
        doRemoveAt(index)
        undo.push(edit)
    }

    fun remove(editor: E) {
        val idx = editors.now.indexOf(editor)
        removeAt(idx)
    }

    /**
     * Is called whenever a new editor is added to the list of editors forming this [ListEditor]
     */
    protected open fun editorAdded(editor: E, index: Int) {}

    /**
     * Is called whenever an editor removed from the list of editors forming this [ListEditor]
     */
    protected open fun editorRemoved(editor: E, index: Int) {}

    private fun doAddAt(index: Int, editor: E, notify: Boolean = true) {
        val emptyBefore = emptyNow()
        _editors.now.add(index, editor)
        child(editor)
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
            editors.now[i].setAccessor(IndexAccessor(index))
        }
    }

    private fun doRemoveAt(index: Int) {
        val old = _editors.now.removeAt(index)
        updateIndicesFrom(index)
        views { removed(index) }
        if (emptyNow()) views { empty() }
        editorRemoved(old, index)
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