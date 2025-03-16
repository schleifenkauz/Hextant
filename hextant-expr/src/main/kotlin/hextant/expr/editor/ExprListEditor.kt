/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.ListEditor
import hextant.expr.Expr
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@ProvideFeature
@Serializable
class ExprListEditor : ListEditor<@Contextual Expr?, ExprEditor<Expr>>() {
    override fun createEditor(): ExprEditor<Expr> = ExprExpander()

    override fun supportsCopyPaste(): Boolean = true
}