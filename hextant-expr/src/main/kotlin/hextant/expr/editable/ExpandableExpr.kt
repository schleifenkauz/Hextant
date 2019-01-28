/**
 *@author Nikolaus Knop
 */

package hextant.expr.editable

import hextant.Editable
import hextant.core.editable.Expandable
import hextant.expr.edited.Expr

class ExpandableExpr : Expandable<Expr, Editable<Expr>>(), EditableExpr<Expr>