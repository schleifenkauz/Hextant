/**
 *@author Nikolaus Knop
 */

package hextant.expr.editable

import hextant.*
import hextant.base.AbstractEditable
import hextant.expr.editable.EditableOperatorApplication.Serial
import hextant.expr.edited.Operator
import hextant.expr.edited.OperatorApplication
import kserial.*
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

@SerializableWith(Serial::class)
class EditableOperatorApplication(
    val editableOperator: EditableOperator,
    val editableOp1: ExpandableExpr,
    val editableOp2: ExpandableExpr
) : AbstractEditable<OperatorApplication>(),
    EditableExpr<OperatorApplication> {
    constructor(operator: Operator) : this() {
        editableOperator.text.set(operator.name)
    }

    constructor() : this(EditableOperator(), ExpandableExpr(), ExpandableExpr())

    constructor(context: Context, edited: OperatorApplication) : this(
        EditableOperator(edited.operator),
        ExpandableExpr(edited.op1, context),
        ExpandableExpr(edited.op2, context)
    )

    override val result: RResult<OperatorApplication> =
        binding<CompileResult<OperatorApplication>>(
            dependencies(editableOp1.result, editableOp2.result, editableOperator.result)
        ) {
            val operator = editableOperator.result.now.ifErr { return@binding ChildErr }
            val op1 = editableOp1.result.now.ifErr { return@binding ChildErr }
            val op2 = editableOp2.result.now.ifErr { return@binding ChildErr }
            Ok(OperatorApplication(op1, op2, operator))
        }

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