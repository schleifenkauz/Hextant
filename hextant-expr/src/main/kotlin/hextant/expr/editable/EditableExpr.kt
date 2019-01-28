/**
 *@author Nikolaus Knop
 */

package hextant.expr.editable

import hextant.Editable
import hextant.expr.edited.Expr

interface EditableExpr<out E : Expr> : Editable<E>