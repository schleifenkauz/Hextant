/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.core.Editor
import hextant.lisp.SExpr

interface SExprEditor<E : SExpr> : Editor<E>, RuntimeScopeAware {
    override fun onInitParent(parent: Editor<*>) {
        initializeScope(parent as? RuntimeScopeAware)
    }
}