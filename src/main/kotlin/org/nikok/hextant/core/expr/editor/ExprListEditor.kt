/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.list.EditableList
import org.nikok.hextant.core.list.ListEditor

class ExprListEditor(
    list: EditableList<Expr, Editable<Expr>>,
    platform: HextantPlatform
) : ListEditor<Editable<Expr>>(list, platform) {
    override fun createNewEditable(): Editable<Expr> = ExpandableExpr()
}