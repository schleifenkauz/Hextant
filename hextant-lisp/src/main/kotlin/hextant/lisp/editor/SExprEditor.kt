/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.core.Editor
import hextant.lisp.SExpr

interface SExprEditor : Editor<SExpr> {
    val scope: RuntimeScopeEditor
}