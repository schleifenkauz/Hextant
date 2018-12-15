/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.reaktive.value.now

class ExpandableExpr : Expandable<Expr, Editable<Expr>>() {
    override fun accepts(child: Editable<*>): Boolean = child.edited.now is Expr
}