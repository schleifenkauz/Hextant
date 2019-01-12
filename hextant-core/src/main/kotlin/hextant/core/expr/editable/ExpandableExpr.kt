/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editable

import hextant.Editable
import hextant.core.editable.Expandable
import hextant.core.expr.edited.Expr

class ExpandableExpr : Expandable<Expr, Editable<Expr>>(), EditableExpr<Expr>