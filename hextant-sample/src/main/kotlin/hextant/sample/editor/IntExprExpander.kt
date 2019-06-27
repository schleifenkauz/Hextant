/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.*
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.sample.ast.IntExpr
import hextant.sample.ast.IntOperator
import hextant.sample.ast.IntOperator.*
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

class IntExprExpander(context: Context) : ConfiguredExpander<IntExpr, Editor<IntExpr>>(config, context),
                                          IntExprEditor {
    override val expr: ReactiveValue<IntExpr?>
        get() = result.map { it.orNull() }

    override fun accepts(editor: Editor<*>): Boolean = editor is IntExprEditor

    companion object {
        val config = ExpanderConfig<Editor<IntExpr>>().apply {
            registerConstant("+") { context -> createOperatorApplication(Plus, context) }
            registerConstant("-") { context -> createOperatorApplication(Minus, context) }
            registerConstant("*") { context -> createOperatorApplication(Times, context) }
            registerConstant("/") { context -> createOperatorApplication(Div, context) }
            registerConstant("dec") { context -> IntLiteralEditor(context) }
            registerInterceptor { text, context ->
                val i = text.toIntOrNull() ?: return@registerInterceptor null
                IntLiteralEditor(context).also {
                    it.setText(i.toString())
                }
            }
        }

        private fun createOperatorApplication(operator: IntOperator, context: Context) =
            IntOperatorApplicationEditor(context).apply {
                op.setText(operator.name)
            }
    }
}