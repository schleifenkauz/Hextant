/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.Operator
import org.nikok.hextant.core.expr.edited.OperatorApplication
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.*
import org.nikok.reaktive.value.binding.binding

class EditableOperatorApplication(override val parent: Editable<*>? = null) : Editable<OperatorApplication> {
    constructor(operator: Operator, parent: Editable<*>? = null) : this(parent) {
        editableOperator.editableText.text.set(operator.name)
    }

    val editableOperator = EditableOperator()

    val editableOp1 = ExpandableExpr()

    val editableOp2 = ExpandableExpr()

    override val edited: ReactiveValue<OperatorApplication?> =
            binding<OperatorApplication?>(
                "operator application",
                dependencies(editableOp1.edited, editableOp2.edited, editableOperator.edited)
            ) {
                val operator = editableOperator.edited.now ?: return@binding null
                val op1 = editableOp1.edited.now ?: return@binding null
                val op2 = editableOp2.edited.now ?: return@binding null
                OperatorApplication(op1, op2, operator)
            }


    override val isOk: ReactiveBoolean = edited.map("is $this ok") { it != null }
}