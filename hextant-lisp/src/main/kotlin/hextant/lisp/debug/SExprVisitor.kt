/**
 * @author Nikolaus Knop
 */

package hextant.lisp.debug

import hextant.lisp.editor.SExprEditor

interface SExprVisitor {
    fun enter(expr: SExprEditor<*>): Boolean

    fun exit(expr: SExprEditor<*>): SExprEditor<*>
}