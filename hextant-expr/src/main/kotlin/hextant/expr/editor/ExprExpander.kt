/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.Editable
import hextant.completion.CompletionFactory
import hextant.completion.CompletionStrategy
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.expr.editable.*
import hextant.expr.edited.Expr
import hextant.expr.edited.Operator.*
import reaktive.value.now

class ExprExpander(
    editable: ExpandableExpr,
    context: Context
) : ConfiguredExpander<Editable<Expr>, ExpandableExpr>(config, editable, context, completer),
    ExprEditor {
    override val expr: Expr?
        get() = editable.editable.now?.edited?.now

    companion object {
        val config = ExpanderConfig<Editable<Expr>>().apply {
            registerConstant("dec") { EditableIntLiteral() }
            registerConstant("+") { EditableOperatorApplication(Plus) }
            registerConstant("-") { EditableOperatorApplication(Minus) }
            registerConstant("*") { EditableOperatorApplication(Times) }
            registerConstant("/") { EditableOperatorApplication(Div) }
            registerConstant("sum") { EditableSum() }
            registerInterceptor {
                val int = it.toIntOrNull()
                if (int != null) EditableIntLiteral(int)
                else null
            }
        }

        val completer = config.completer(CompletionStrategy.simple, CompletionFactory.simple())
    }
}