/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.expr.Operator
import hextant.expr.OperatorApplication
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class OperatorApplicationEditor(
    context: Context,
    text: String
) : CompoundEditor<OperatorApplication>(context), ExprEditor<OperatorApplication> {
    constructor(operator: Operator, context: Context) : this(context, operator.name)
    constructor(context: Context) : this(context, "")

    val operator by child(OperatorEditor(context, text))
    val operand1 by child(ExprExpander(context))
    val operand2 by child(ExprExpander(context))

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