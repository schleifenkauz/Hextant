/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.ListEditor
import hextant.expr.Expr

@ProvideFeature
class ExprListEditor : ListEditor<Expr?, ExprEditor<Expr>>() {
    override fun createEditor(): ExprEditor<Expr> = ExprExpander()

    override fun supportsCopyPaste(): Boolean = true
}