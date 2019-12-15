/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.expr.edited.Operator
import hextant.expr.edited.OperatorApplication
import kserial.CompoundSerializable
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class OperatorApplicationEditor(
    op: OperatorEditor,
    opnd1: ExprExpander,
    opnd2: ExprExpander,
    context: Context
) : AbstractEditor<OperatorApplication, Any>(context), ExprEditor<OperatorApplication>, CompoundSerializable {
    val operator = op.moveTo(context)
    val operand1 = opnd1.moveTo(context)
    val operand2 = opnd2.moveTo(context)

    constructor(operator: Operator, context: Context) : this(context) {
        this.operator.setText(operator.name)
    }

    init {
        children(operand1, operator, operand2)
    }

    constructor(context: Context) : this(
        OperatorEditor(context),
        ExprExpander(context),
        ExprExpander(context),
        context
    )

    constructor(context: Context, edited: OperatorApplication) : this(
        OperatorEditor(context, edited.operator),
        ExprExpander(edited.op1, context),
        ExprExpander(edited.op2, context),
        context
    )

    override fun copyForImpl(context: Context): OperatorApplicationEditor {
        return OperatorApplicationEditor(
            operator.copyFor(context),
            operand1.copyFor(context),
            operand2.copyFor(context),
            context
        )
    }

    override fun components(): Sequence<Any> = sequenceOf(operator, operand1, operand2)

    override val result: EditorResult<OperatorApplication> =
        binding<CompileResult<OperatorApplication>>(
            dependencies(operand1.result, operand2.result, operator.result)
        ) {
            val operator = operator.result.now.ifErr { return@binding ChildErr }
            val op1 = operand1.result.now.ifErr { return@binding ChildErr }
            val op2 = operand2.result.now.ifErr { return@binding ChildErr }
            Ok(OperatorApplication(op1, op2, operator))
        }
}