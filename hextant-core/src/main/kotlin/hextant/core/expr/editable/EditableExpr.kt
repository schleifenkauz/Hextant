/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editable

import hextant.Editable
import hextant.core.expr.edited.Expr

interface EditableExpr<out E : Expr> : Editable<E>