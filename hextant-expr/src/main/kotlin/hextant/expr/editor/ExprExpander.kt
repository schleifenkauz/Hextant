/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.createEditor
import hextant.expr.edited.Expr
import hextant.expr.edited.Operator.*

class ExprExpander(
    context: Context, editor: ExprEditor<Expr>?
) : ConfiguredExpander<Expr, ExprEditor<Expr>>(config, context, editor), ExprEditor<Expr> {
    constructor(context: Context) : this(context, null)

    constructor(edited: Expr, context: Context) : this(
        context,
        context.createEditor(edited) as ExprEditor<Expr>
    )

    companion object {
        val config = ExpanderConfig<ExprEditor<Expr>>().apply {
            registerConstant("dec") { context -> IntLiteralEditor(context) }
            registerConstant("+") { context -> OperatorApplicationEditor(Plus, context) }
            registerConstant("-") { context -> OperatorApplicationEditor(Minus, context) }
            registerConstant("*") { context -> OperatorApplicationEditor(Times, context) }
            registerConstant("/") { context -> OperatorApplicationEditor(Div, context) }
            registerConstant("sum") { context -> SumEditor(context) }
            registerInterceptor { text, context ->
                val int = text.toIntOrNull()
                if (int != null) IntLiteralEditor(context, int.toString())
                else null
            }
        }
    }
}