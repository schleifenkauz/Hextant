/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.Expr

interface EditableExpr<out E : Expr> : Editable<E>