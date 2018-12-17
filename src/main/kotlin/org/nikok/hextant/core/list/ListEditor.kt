/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor

abstract class ListEditor<E : Editable<*>>(
    private val list: EditableList<*, E>,
    platform: HextantPlatform
) : AbstractEditor<EditableList<*, E>, ListEditorView>(list, platform) {
    override fun viewAdded(view: ListEditorView) {
        for ((idx, child) in list.editableList.now.withIndex()) {
            view.onGuiThread {
                view.added(child, idx)
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
        return editable
    }

    fun removeAt(idx: Int) {
        val removed = list.editableList.now.removeAt(idx)
        removed.moveTo(null)
        views {
            removed(idx)
        }
    }
}