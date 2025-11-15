/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.ListEditor
import hextant.core.editor.defaultState
import hextant.expr.Expr
import kotlinx.serialization.Contextual

@ProvideFeature
class ExprListEditor : ListEditor<@Contextual Expr?, ExprEditor<@Contextual Expr>>() {
    override fun createEditor(): ExprEditor<Expr> = ExprExpander().defaultState()

    override fun supportsCopyPaste(): Boolean = true
}