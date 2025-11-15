/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.core.editor.defaultState
import hextant.expr.Operator
import hextant.expr.OperatorApplication
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.ReactiveValue

@ProvideFeature
class OperatorApplicationEditor() : CompoundEditor<@Contextual OperatorApplication?>(), ExprEditor<OperatorApplication> {
    val operator by child(OperatorEditor().defaultState())
    val operand1 by child(ExprExpander().defaultState())
    val operand2 by child(ExprExpander().defaultState())

    constructor(operator: Operator) : this() {
        this.operator.setInitialText(operator.toString())
    }

    @Transient
    override lateinit var result: ReactiveValue<OperatorApplication?>
        private set

    override fun doInitialize() {
        super.doInitialize()
        result = composeResult { OperatorApplication(operand1.now, operand2.now, operator.now) }
    }
}