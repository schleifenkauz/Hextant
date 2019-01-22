/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editable

import hextant.base.AbstractEditable
import hextant.lisp.*
import reaktive.dependencies
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding

class EditableApply : AbstractEditable<Apply>(), EditableSExpr<Apply> {
    val editableExpressions = EditableSExprList()

    private val expressions get() = editableExpressions.editedList

    @Suppress("UNCHECKED_CAST")
    override val edited: ReactiveValue<Apply?> = binding<Apply?>(dependencies(expressions)) {
        if (expressions.now.all { it != null })
            Apply(SinglyLinkedList.fromList(expressions.now as List<SExpr>))
        else null
    }
}