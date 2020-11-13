/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.context.Context
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.expr.Expr
import hextant.expr.IntLiteral
import hextant.expr.Operator.*
import validated.*

class ExprExpander(
    context: Context, editor: ExprEditor<Expr>? = null
) : ConfiguredExpander<Expr, ExprEditor<Expr>>(config, context, editor), ExprEditor<Expr> {
    override fun compile(token: String): Validated<Expr> =
        token.toIntOrNull().validated { invalid("Invalid int literal $token") }.map { IntLiteral(it) }

    companion object {
        val config = ExpanderConfig<ExprEditor<Expr>>().apply {
            registerKey("num") { context -> IntLiteralEditor(context) }
            registerKey("+") { context -> OperatorApplicationEditor(Plus, context) }
            registerKey("-") { context -> OperatorApplicationEditor(Minus, context) }
            registerKey("*") { context -> OperatorApplicationEditor(Times, context) }
            registerKey("/") { context -> OperatorApplicationEditor(Div, context) }
            registerKey("sum") { context -> SumEditor(context) }
            registerInterceptor { item: Int, ctx: Context -> IntLiteralEditor(ctx, item.toString()) }
            registerInterceptor { text, context ->
                val int = text.toIntOrNull()
                if (int != null) IntLiteralEditor(context, int.toString())
                else null
            }
        }
    }
}