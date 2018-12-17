/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.list.EditableList

class EditableExprList : EditableList<Expr, Editable<Expr>>(EditableExpr::class)