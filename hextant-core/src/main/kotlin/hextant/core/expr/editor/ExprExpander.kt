/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.Context
import hextant.Editable
import hextant.core.editor.ConfiguredExpander
import hextant.core.editor.ExpanderConfig
import hextant.core.expr.editable.*
import hextant.core.expr.edited.Expr
import hextant.core.expr.edited.Operator.*
import reaktive.value.now

class ExprExpander(
    editable: ExpandableExpr,
    context: Context
) : ConfiguredExpander<Editable<Expr>, ExpandableExpr>(config, editable, context), ExprEditor {
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
            registerInterceptor { it ->
                val int = it.toIntOrNull()
                if (int != null) EditableIntLiteral(int)
                else null
            }
        }
    }
}