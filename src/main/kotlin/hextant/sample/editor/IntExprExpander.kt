/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.Editable
import hextant.core.editable.Expandable
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntOperator
import hextant.sample.ast.IntOperator.*
import hextant.sample.editable.EditableIntLiteral
import hextant.sample.editable.EditableIntOperatorApplication

class IntExprExpander(
    edited: Expandable<*, Editable<IntExpr>>,
    context: Context
) : ConfiguredExpander<Editable<IntExpr>>(config, edited, context) {
    companion object {
        val config = ExpanderConfig<Editable<IntExpr>>().apply {
            registerConstant("+") { createOperatorApplication(Plus) }
            registerConstant("-") { createOperatorApplication(Minus) }
            registerConstant("*") { createOperatorApplication(Times) }
            registerConstant("/") { createOperatorApplication(Div) }
            registerConstant("dec") { EditableIntLiteral() }
            registerInterceptor { text ->
                val i = text.toIntOrNull() ?: return@registerInterceptor null
                EditableIntLiteral().also {
                    it.text.set(i.toString())
                }
            }
        }

        private fun createOperatorApplication(operator: IntOperator) =
            EditableIntOperatorApplication().apply {
                op.text.set(operator.name)
            }
    }
}