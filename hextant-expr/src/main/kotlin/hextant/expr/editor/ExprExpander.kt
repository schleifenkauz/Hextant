/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.expr.Expr
import hextant.expr.IntLiteral
import hextant.expr.Operator.*

class ExprExpander : ConfiguredExpander<Expr?, ExprEditor<Expr>>(), ExprEditor<Expr> {
    init {
        configure(config)
    }

    override fun compile(token: String): Expr? = token.toIntOrNull()?.let { IntLiteral(it) }

    companion object {
        val config = ExpanderConfig<ExprEditor<Expr>>().apply {
            registerKey("num") { _ -> IntLiteralEditor() }
            registerKey("+") { OperatorApplicationEditor(Plus) }
            registerKey("-") { OperatorApplicationEditor(Minus) }
            registerKey("*") { OperatorApplicationEditor(Times) }
            registerKey("/") { OperatorApplicationEditor(Div) }
            registerKey("sum") { context -> SumEditor() }
            registerInterceptor { text, _ ->
                val int = text.toIntOrNull()
                if (int != null) IntLiteralEditor(IntLiteral(int))
                else null
            }
        }
    }
}