/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.ListEditor
import hextant.lisp.SExpr

class SExprListEditor(context: Context) : ListEditor<SExpr, SExprEditor<*>>(context) {
    override fun createEditor(): SExprEditor<*> = SExprExpander(context)
}