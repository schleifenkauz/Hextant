/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.core.list.EditableList
import hextant.core.list.ListEditor
import hextant.expr.editable.*
import hextant.expr.edited.Expr

class ExprListEditor(
    list: EditableExprList,
    context: Context
) : ListEditor<Editable<Expr>, EditableExprList>(list, context) {
    override fun createNewEditable(): Editable<Expr> = ExpandableExpr()

    override fun accepts(child: Editor<*>): Boolean = child.editable is EditableExpr<*>
}