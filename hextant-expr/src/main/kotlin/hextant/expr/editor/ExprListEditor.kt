/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.ListEditor
import hextant.expr.Expr

@ProvideFeature
class ExprListEditor(context: Context) : ListEditor<Expr?, ExprEditor<Expr>>(context) {
    override fun createEditor(): ExprEditor<Expr> = ExprExpander(context)

    override fun supportsCopyPaste(): Boolean = true
}