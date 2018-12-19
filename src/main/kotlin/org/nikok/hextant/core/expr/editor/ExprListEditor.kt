/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Context
import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.list.EditableList
import org.nikok.hextant.core.list.ListEditor

class ExprListEditor(
    list: EditableList<Expr, Editable<Expr>>,
    context: Context
) : ListEditor<Editable<Expr>>(list, context) {
    override fun createNewEditable(): Editable<Expr> = ExpandableExpr()
}