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
        val editor = context.getEditor(editable)
        editor.moveTo(this)
        list.editableList.now.add(idx, editable)
        views {
            added(editable, idx)
        }
        if (!empty) {
            views {
                notEmpty()
            }
        }
        return editable
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
}