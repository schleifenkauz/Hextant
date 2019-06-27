/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.Editor
import hextant.expr.edited.Expr

interface ExprEditor<out E : Expr> : Editor<E> {
    override fun copyFor(context: Context): ExprEditor<E>

    override fun supportsCopy(): Boolean = true
}