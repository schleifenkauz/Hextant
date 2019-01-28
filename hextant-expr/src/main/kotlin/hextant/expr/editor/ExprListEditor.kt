/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.core.list.EditableList
import hextant.core.list.ListEditor
import hextant.expr.editable.EditableExpr
import hextant.expr.editable.ExpandableExpr
import hextant.expr.edited.Expr

class ExprListEditor(
    list: EditableList<Expr, Editable<Expr>>,
    context: Context
) : ListEditor<Editable<Expr>>(list, context) {
    override fun createNewEditable(): Editable<Expr> = ExpandableExpr()

    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableExpr<*>
}