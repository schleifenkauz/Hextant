/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.Apply
import hextant.lisp.SinglyLinkedList
import reaktive.dependencies
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding

class EditableApply : AbstractEditable<Apply>(), EditableSExpr<Apply> {
    val editableExpressions = EditableSExprList()

    override val edited: ReactiveValue<Apply?> =
        binding<Apply?>(dependencies(editableExpressions.editableList)) {
            val exprs = editableExpressions.editedList.now
            if (exprs.any { it == null }) return@binding null
            Apply(SinglyLinkedList.fromList(exprs.requireNoNulls()))
        }
}