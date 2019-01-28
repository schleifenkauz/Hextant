/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.core.editable.EditableToken
import hextant.expr.view.FXTokenEditorView

class DoubleLiteralEditorControl(editable: EditableToken<Any>, context: Context) :
    FXTokenEditorView(editable, context) {
    init {
        root.styleClass.add("lisp-double-literal")
    }
}