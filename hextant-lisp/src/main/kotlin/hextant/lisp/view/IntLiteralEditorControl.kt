/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.lisp.editable.EditableIntLiteral

class IntLiteralEditorControl(
    editable: EditableIntLiteral,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        root.styleClass.add("lisp-int-literal")
    }
}