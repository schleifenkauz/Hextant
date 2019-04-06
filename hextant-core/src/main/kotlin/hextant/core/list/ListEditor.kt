/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import hextant.*
import hextant.base.ParentEditor
import hextant.undo.*

abstract class ListEditor<E : Editable<*>, L: EditableList<*, E>>(
    private val list: L,
    private val context: Context
) : ParentEditor<L, ListEditorView>(list, context) {
    init {
        for (child in list.editableList.now) {
            val editor = context.getEditor(child)
            editor.moveTo(this)
        }
    }

    private val undo = context[UndoManager]

    private val empty get() = list.editableList.now.isEmpty()

    override fun viewAdded(view: ListEditorView) {
        if (empty) {
            view.onGuiThread {
                view.empty()
            }
        } else {
            for ((idx, child) in list.editableList.now.withIndex()) {
                view.onGuiThread {
                    view.added(child, idx)
                }
            }
        }
    }

    protected abstract fun createNewEditable(): E

    fun add(idx: Int): E {
        val editable = createNewEditable()
        context.runLater {
            val edit = doAdd(idx, editable)
            undo.push(edit)
        }
        return editable
    }

    fun add(idx: Int, editable: E) {
        context.runLater {
            val edit = doAdd(idx, editable)
            undo.push(edit)
        }
    }

    private fun doAdd(idx: Int, editable: E): Edit {
        val emptyBefore = empty
        val editor = context.getEditor(editable)
        editor.moveTo(this)
        list.editableList.now.add(idx, editable)
        views {
            added(editable, idx)
            if (emptyBefore) notEmpty()
        }
        return AddEdit(idx, editable)
    }

    private inner class AddEdit(private val index: Int, private val editable: E) : AbstractEdit() {
        override fun doRedo() {
            doAdd(index, editable)
        }

        override fun doUndo() {
            doRemoveAt(index)
        }

        override val actionDescription: String
            get() = "Add element"
    }

    fun removeAt(idx: Int) {
        context.runLater {
            val edit = doRemoveAt(idx)
            undo.push(edit)
        }
    }

    private fun doRemoveAt(idx: Int): Edit {
        val removed = list.editableList.now.removeAt(idx)
        val editor = context.getEditor(removed)
        editor.moveTo(null)
        views {
            removed(idx)
        }
        if (empty) {
            views {
                empty()
            }
        }
        return RemoveEdit(idx, removed)
    }

    private inner class RemoveEdit(private val index: Int, private val editable: E) : AbstractEdit() {
        override fun doRedo() {
            doRemoveAt(index)
        }

        override fun doUndo() {
            doAdd(index, editable)
        }

        override val actionDescription: String
            get() = "Remove element"
    }

    fun clear() {
        context.runLater {
            val editables = list.editableList.now
            for (editable in editables) {
                val editor = context.getEditor(editable)
                editor.moveTo(null)
                views {
                    removed(0)
                }
            }
            editables.clear()
        }
        views {
            empty()
        }
    }
}