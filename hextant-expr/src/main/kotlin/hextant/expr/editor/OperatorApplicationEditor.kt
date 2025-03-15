/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.expr.Operator
import hextant.expr.OperatorApplication
import reaktive.value.ReactiveValue

@ProvideFeature
class OperatorApplicationEditor() : CompoundEditor<OperatorApplication?>(), ExprEditor<OperatorApplication> {
    val operator by child(OperatorEditor())
    val operand1 by child(ExprExpander())
    val operand2 by child(ExprExpander())

    constructor(operator: Operator) : this() {
        this.operator.setInitialText(operator.toString())
    }

    override val result: ReactiveValue<OperatorApplication?> =
        composeResult { OperatorApplication(operand1.now, operand2.now, operator.now) }
}