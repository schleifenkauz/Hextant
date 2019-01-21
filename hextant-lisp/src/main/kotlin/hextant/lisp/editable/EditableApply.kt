/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.Apply
import hextant.lisp.SinglyLinkedList
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.binding.binding

class EditableApply : AbstractEditable<Apply>(), EditableSExpr<Apply> {
    val editableExpressions = EditableSExprList()

    override val edited: ReactiveValue<Apply?> =
        binding<Apply?>("edited", dependencies(editableExpressions.editableList)) {
            val exprs = editableExpressions.editedList.now
            if (exprs.any { it == null }) return@binding null
            Apply(SinglyLinkedList.fromList(exprs.requireNoNulls()))
        }
}