/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editable

import hextant.Editable
import hextant.core.expr.edited.Operator
import hextant.core.expr.edited.OperatorApplication
import kserial.*
import reaktive.dependencies
import reaktive.value.*
import reaktive.value.binding.binding
import reaktive.value.binding.map

@SerializableWith(EditableOperatorApplication.Serial::class)
class EditableOperatorApplication(
    val editableOperator: EditableOperator = EditableOperator(),
    val editableOp1: ExpandableExpr = ExpandableExpr(),
    val editableOp2: ExpandableExpr = ExpandableExpr()
) : Editable<OperatorApplication>, EditableExpr<OperatorApplication> {
    constructor(operator: Operator) : this() {
        editableOperator.text.set(operator.name)
    }

    override val edited: ReactiveValue<OperatorApplication?> =
        binding<OperatorApplication?>(
            dependencies(editableOp1.edited, editableOp2.edited, editableOperator.edited)
        ) {
            val operator = editableOperator.edited.now ?: return@binding null
            val op1 = editableOp1.edited.now ?: return@binding null
            val op2 = editableOp2.edited.now ?: return@binding null
            OperatorApplication(op1, op2, operator)
        }

    override val isOk: ReactiveBoolean = edited.map { it != null }

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