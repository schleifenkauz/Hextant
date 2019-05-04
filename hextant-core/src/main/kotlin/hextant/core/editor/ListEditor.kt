/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.view.ListEditorView
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import reaktive.dependencies
import reaktive.list.ReactiveList
import reaktive.list.binding.values
import reaktive.list.reactiveList
import reaktive.value.binding.binding

abstract class ListEditor<R : Any, E : Editor<R>>(
    context: Context
) : AbstractEditor<List<R>, ListEditorView>(context) {
    private val undo = context[UndoManager]

    private val _editors = reactiveList<E>()

    val editors: ReactiveList<E> get() = _editors

    val results = editors.map { it.result }.values()

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

    fun addAt(index: Int) {
        val editor = createEditor()
        addAt(index, editor)
    }

    fun addAt(index: Int, editor: E) {
        val edit = AddEdit(index, editor)
        doAddAt(index, editor)
        undo.push(edit)
    }

    fun removeAt(index: Int) {
        val editor = editors.now[index]
        val edit = RemoveEdit(index, editor)
        doRemoveAt(index)
        undo.push(edit)
    }

    private fun doAddAt(index: Int, editor: E) {
        val emptyBefore = emptyNow()
        _editors.now.add(index, editor)
        child(editor)
        views {
            if (emptyBefore) notEmpty()
            added(editor, index)
        }
    }

    private fun doRemoveAt(index: Int) {
        _editors.now.removeAt(index)
        views { removed(index) }
        if (emptyNow()) views { empty() }
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
        inline fun <reified R : Any> forType(context: Context): ListEditor<R, Editor<R>> =
            object : ListEditor<R, Editor<R>>(context) {
                override fun createEditor(): Editor<R> = context.createEditor(R::class)
            }
    }
}