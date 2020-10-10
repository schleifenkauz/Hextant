/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.context.Context
import hextant.core.editor.ListEditor
import hextant.lisp.SExpr

class SymbolListEditor(context: Context, val scope: RuntimeScopeEditor) : ListEditor<SExpr, SymbolEditor>(context) {
    override fun createEditor(): SymbolEditor? = SymbolEditor(context, scope)
}