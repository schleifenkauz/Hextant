/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.core.editor.ConfiguredExpander
import org.nikok.hextant.core.editor.ExpanderConfig
import org.nikok.hextant.core.expr.editable.*
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.edited.Operator.*
import org.nikok.reaktive.value.now

class ExprExpander(
    editable: ExpandableExpr
) : ConfiguredExpander<Editable<Expr>, ExprExpander>(config, editable), ExprEditor {
    override val expr: Expr?
        get() = editable.editable.now?.edited?.now

    companion object {
        val config = ExpanderConfig<Editable<Expr>, ExprExpander>().apply {
            registerConstant("dec") { EditableIntLiteral(parent = editable) }
            registerConstant("+") { EditableOperatorApplication(Plus, parent = editable) }
            registerConstant("-") { EditableOperatorApplication(Minus, parent = editable) }
            registerConstant("*") { EditableOperatorApplication(Times, parent = editable) }
            registerConstant("/") { EditableOperatorApplication(Div, parent = editable) }
            registerInterceptor {
                val int = it.toIntOrNull()
                if (int != null) EditableIntLiteral(int, parent = editable)
                else null
            }
        }
    }
}