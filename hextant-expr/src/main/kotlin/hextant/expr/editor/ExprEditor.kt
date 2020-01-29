/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Editor
import hextant.expr.edited.Expr

interface ExprEditor<out E : Expr> : Editor<E> {
    override fun supportsCopyPaste(): Boolean = true
}