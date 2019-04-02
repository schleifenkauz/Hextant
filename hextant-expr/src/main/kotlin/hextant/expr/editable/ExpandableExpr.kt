/**
 *@author Nikolaus Knop
 */

package hextant.expr.editable

import hextant.*
import hextant.core.editable.Expandable
import hextant.expr.edited.Expr

class ExpandableExpr() : Expandable<Expr, Editable<Expr>>(), EditableExpr<Expr> {
    constructor(content: EditableExpr<*>) : this() {
        setContent(content)
    }

    constructor(expr: Expr, context: Context) : this(context.getEditable(expr) as EditableExpr<*>)
}