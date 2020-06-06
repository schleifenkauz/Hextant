/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.base.CompoundEditor
import hextant.core.editor.composeResult
import hextant.expr.Operator
import hextant.expr.OperatorApplication
import validated.reaktive.ReactiveValidated

class OperatorApplicationEditor(
    context: Context,
    text: String
) : CompoundEditor<OperatorApplication>(context), ExprEditor<OperatorApplication> {
    constructor(operator: Operator, context: Context) : this(context, operator.name)
    constructor(context: Context) : this(context, "")

    val operator by child(OperatorEditor(context, text))
    val operand1 by child(ExprExpander(context))
    val operand2 by child(ExprExpander(context))

    override val result: ReactiveValidated<OperatorApplication> = composeResult(operand1, operand2, operator)
}