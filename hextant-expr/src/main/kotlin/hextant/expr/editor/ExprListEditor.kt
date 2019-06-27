/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.core.editor.ListEditor
import hextant.expr.edited.Expr

class ExprListEditor(context: Context) : ListEditor<Expr, ExprEditor<Expr>>(context) {
    override fun createEditor(): ExprEditor<Expr> = ExprExpander(context)

    override fun supportsCopy(): Boolean = true

    override fun copyFor(context: Context): ExprListEditor = super.copyFor(context) as ExprListEditor
}