/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import hextant.Context
import hextant.Editable
import hextant.core.base.AbstractEditor

abstract class ListEditor<E : Editable<*>>(
    private val list: EditableList<*, E>,
    context: Context
) : AbstractEditor<EditableList<*, E>, ListEditorView>(list, context) {
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
        editable.moveTo(list)
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
        removed.moveTo(null)
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