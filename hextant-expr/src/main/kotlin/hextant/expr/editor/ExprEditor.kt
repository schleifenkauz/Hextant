/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.core.Editor
import hextant.expr.Expr
import kotlinx.serialization.Polymorphic

@Polymorphic
interface ExprEditor<out E : Expr> : Editor<E?>