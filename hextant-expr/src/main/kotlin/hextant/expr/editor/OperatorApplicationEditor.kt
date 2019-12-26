/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.expr.edited.Operator
import hextant.expr.edited.OperatorApplication
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class OperatorApplicationEditor(
    context: Context,
    op: OperatorEditor,
    opnd1: ExprExpander,
    opnd2: ExprExpander
) : CompoundEditor<OperatorApplication>(context), ExprEditor<OperatorApplication> {
    val operator by child(op, context)
    val operand1 by child(opnd1, context)
    val operand2 by child(opnd2, context)

    constructor(operator: Operator, context: Context) : this(context) {
        this.operator.setText(operator.name)
    }

    constructor(context: Context) : this(
        context,
        OperatorEditor(context),
        ExprExpander(context),
        ExprExpander(context)
    )

    constructor(context: Context, edited: OperatorApplication) : this(
        context,
        OperatorEditor(context, edited.operator),
        ExprExpander(edited.op1, context),
        ExprExpander(edited.op2, context)
    )

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