/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.*
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.binding.binding

class EditableListExpr : AbstractEditable<ListExpr>(), EditableSExpr<ListExpr> {
    val editableList = EditableSExprList()

    @Suppress("UNCHECKED_CAST")
    override val edited: ReactiveValue<ListExpr?>
        get() = binding<ListExpr?>("edited", dependencies(editableList.editedList)) {
            val exprs = editableList.editedList.now
            if (exprs.any { it == null }) return@binding null
            ListExpr(SinglyLinkedList.fromList(exprs) as SinglyLinkedList<SExpr>)
        }
}