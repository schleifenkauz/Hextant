/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.context.Context
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.expr.Expr
import hextant.expr.Operator.*

class ExprExpander(
    context: Context, editor: ExprEditor<Expr>?
) : ConfiguredExpander<Expr, ExprEditor<Expr>>(config, context, editor), ExprEditor<Expr> {
    constructor(context: Context) : this(context, null)

    companion object {
        val config = ExpanderConfig<ExprEditor<Expr>, Context>().apply {
            registerKey("dec") { context -> IntLiteralEditor(context) }
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