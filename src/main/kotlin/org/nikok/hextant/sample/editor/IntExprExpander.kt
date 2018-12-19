/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.Context
import org.nikok.hextant.Editable
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.editor.ConfiguredExpander
import org.nikok.hextant.core.editor.ExpanderConfig
import org.nikok.hextant.sample.ast.IntExpr
import org.nikok.hextant.sample.ast.IntOperator
import org.nikok.hextant.sample.ast.IntOperator.*
import org.nikok.hextant.sample.editable.EditableIntLiteral
import org.nikok.hextant.sample.editable.EditableIntOperatorApplication

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