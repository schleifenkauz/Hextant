/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.Context
import hextant.Editable
import hextant.core.expr.editable.ExpandableExpr
import hextant.core.expr.edited.Expr
import hextant.core.list.EditableList
import hextant.core.list.ListEditor

class ExprListEditor(
    list: EditableList<Expr, Editable<Expr>>,
    context: Context
) : ListEditor<Editable<Expr>>(list, context) {
    override fun createNewEditable(): Editable<Expr> = ExpandableExpr()
}