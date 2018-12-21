/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editable

import hextant.Editable
import hextant.core.expr.edited.Expr
import hextant.core.list.EditableList

class EditableExprList : EditableList<Expr, Editable<Expr>>(EditableExpr::class)