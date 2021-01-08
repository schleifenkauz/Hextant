/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.core.Editor
import hextant.expr.Expr
import validated.Validated

interface ExprEditor<out E : Expr> : Editor<Validated<E>>