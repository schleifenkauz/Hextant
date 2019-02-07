/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import hextant.*
import hextant.base.ParentEditor

abstract class ListEditor<E : Editable<*>>(
    private val list: EditableList<*, E>,
    private val context: Context
) : ParentEditor<EditableList<*, E>, ListEditorView>(list, context) {
    init {
        for (child in list.editableList.now) {
            val editor = context.getEditor(child)
            editor.moveTo(this)
        }
    }

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
        add(idx, editable)
        return editable
    }

    fun add(idx: Int, editable: E) {
        val emptyBefore = list.editableList.now.isEmpty()
        context.runLater {
            val editor = context.getEditor(editable)
            editor.moveTo(this)
            list.editableList.now.add(idx, editable)
        }
        views {
            added(editable, idx)
            if (emptyBefore) notEmpty()
        }
    }

    fun removeAt(idx: Int) {
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