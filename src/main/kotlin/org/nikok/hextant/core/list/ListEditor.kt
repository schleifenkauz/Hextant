/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import org.nikok.hextant.Editable
import org.nikok.hextant.core.base.AbstractEditor

abstract class ListEditor<E : Editable<*>>(
    private val list: EditableList<*, E>,
    view: ListEditorView
) : AbstractEditor<EditableList<*, E>>(list, view) {
    protected abstract fun createNewEditable(): E

    fun add(idx: Int): E {
        val editable = createNewEditable()
        list.editableList.now.add(idx, editable)
        return editable
    }

    fun remove(editable: E) {
        list.editableList.now.remove(editable)
    }

    fun removeAt(idx: Int) {
        list.editableList.now.removeAt(idx)
    }
}