/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import kserial.*
import org.nikok.hextant.Editable
import org.nikok.hextant.ParentEditable
import org.nikok.hextant.core.expr.edited.Operator
import org.nikok.hextant.core.expr.edited.OperatorApplication
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.*
import org.nikok.reaktive.value.binding.binding

@SerializableWith(EditableOperatorApplication.Serial::class)
class EditableOperatorApplication(
    val editableOperator: EditableOperator = EditableOperator(),
    val editableOp1: ExpandableExpr = ExpandableExpr(),
    val editableOp2: ExpandableExpr = ExpandableExpr()
) : ParentEditable<OperatorApplication, Editable<*>>(), EditableExpr<OperatorApplication> {
    constructor(operator: Operator) : this() {
        editableOperator.text.set(operator.name)
    }

    init {
        editableOp1.moveTo(this)
        editableOperator.moveTo(this)
        editableOp2.moveTo(this)
    }

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

    override fun accepts(child: Editable<*>): Boolean = child is EditableOperator || child is EditableExpr<*>

    object Serial : Serializer<EditableOperatorApplication> {
        override fun serialize(obj: EditableOperatorApplication, output: Output, context: SerialContext) {
            with(output) {
                writeObject(obj.editableOperator, context)
                writeObject(obj.editableOp1, context)
                writeObject(obj.editableOp2, context)
            }
        }

        override fun deserialize(
            cls: Class<EditableOperatorApplication>,
            input: Input,
            context: SerialContext
        ): EditableOperatorApplication = with(input) {
            EditableOperatorApplication(
                readTyped(context)!!,
                readTyped(context)!!,
                readTyped(context)!!
            )
        }
    }
}