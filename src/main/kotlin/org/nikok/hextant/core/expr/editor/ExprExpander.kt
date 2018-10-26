/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.core.editor.*
import org.nikok.hextant.core.expr.editable.*
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.expr.edited.Operator.*
import org.nikok.hextant.core.view.ExpanderView

class ExprExpander(
    editable: ExpandableExpr, view: ExpanderView
) : ConfiguredExpander<Editable<Expr>>(config, view, editable) {
    companion object {
        val config = ExpanderConfig<Editable<Expr>>().apply {
            registerConstant("dec") { EditableIntLiteral() }
            registerConstant("+") { EditableOperatorApplication(Plus) }
            registerConstant("-") { EditableOperatorApplication(Minus) }
            registerConstant("*") { EditableOperatorApplication(Times) }
            registerConstant("/") { EditableOperatorApplication(Div) }
            registerInterceptor {
                val int = it.toIntOrNull()
                if (int != null) EditableIntLiteral(int)
                else null
            }
        }
    }
}