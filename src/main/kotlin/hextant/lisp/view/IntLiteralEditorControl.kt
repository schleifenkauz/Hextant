/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.core.expr.view.FXTokenEditorView
import hextant.lisp.editable.EditableIntLiteral

class IntLiteralEditorControl(
    editable: EditableIntLiteral,
    context: Context
) : FXTokenEditorView(editable, context) {
    init {
        styleClass.add("int-literal")
    }
}