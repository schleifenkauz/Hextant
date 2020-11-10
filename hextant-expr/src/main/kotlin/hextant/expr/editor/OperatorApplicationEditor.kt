/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.expr.Operator
import hextant.expr.OperatorApplication
import reaktive.value.ReactiveValue

@ProvideFeature
class OperatorApplicationEditor(
    context: Context,
    text: String
) : CompoundEditor<OperatorApplication>(context), ExprEditor<OperatorApplication> {
    constructor(operator: Operator, context: Context) : this(context, operator.name)
    constructor(context: Context) : this(context, "")

    val operator by child(OperatorEditor(context, text))
    val operand1 by child(ExprExpander(context))
    val operand2 by child(ExprExpander(context))

    override val result: ReactiveValue<OperatorApplication?> =
        composeResult { OperatorApplication(operand1.now, operand2.now, operator.now) }
}