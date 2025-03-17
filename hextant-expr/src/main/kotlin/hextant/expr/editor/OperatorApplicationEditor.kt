/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.expr.Operator
import hextant.expr.OperatorApplication
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.ReactiveValue

@ProvideFeature
class OperatorApplicationEditor() : CompoundEditor<@Contextual OperatorApplication?>(), ExprEditor<OperatorApplication> {
    val operator by child(OperatorEditor())
    val operand1 by child(ExprExpander())
    val operand2 by child(ExprExpander())

    constructor(operator: Operator) : this() {
        this.operator.setInitialText(operator.toString())
    }

    @Transient
    override lateinit var result: ReactiveValue<OperatorApplication?>
        private set

    override fun doInitialize() {
        result = composeResult { OperatorApplication(operand1.now, operand2.now, operator.now) }
    }
}