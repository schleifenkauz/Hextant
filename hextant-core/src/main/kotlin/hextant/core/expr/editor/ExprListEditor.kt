/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.*
import hextant.core.expr.editable.EditableExpr
import hextant.core.expr.editable.ExpandableExpr
import hextant.core.expr.edited.Expr
import hextant.core.list.EditableList
import hextant.core.list.ListEditor

class ExprListEditor(
    list: EditableList<Expr, Editable<Expr>>,
    context: Context
) : ListEditor<Editable<Expr>>(list, context) {
    override fun createNewEditable(): Editable<Expr> = ExpandableExpr()

    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableExpr<*>
}