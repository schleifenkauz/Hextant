/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Context
import org.nikok.hextant.Editable
import org.nikok.hextant.core.editor.ConfiguredExpander
import org.nikok.hextant.core.editor.ExpanderConfig
import org.nikok.hextant.core.expr.editable.*
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.edited.Operator.*
import org.nikok.reaktive.value.now

class ExprExpander(
    editable: ExpandableExpr,
    context: Context
) : ConfiguredExpander<Editable<Expr>>(config, editable, context), ExprEditor {
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