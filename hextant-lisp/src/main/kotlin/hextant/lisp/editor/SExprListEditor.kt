/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.ListEditor
import hextant.lisp.SExpr

class SExprListEditor(context: Context, val scope: RuntimeScopeEditor) : ListEditor<SExpr, SExprExpander>(context) {
    override fun createEditor(): SExprExpander = SExprExpander(context, scope)
}