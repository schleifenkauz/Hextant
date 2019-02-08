/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.editable.EditableToken
import hextant.core.view.FXTokenEditorView

class DoubleLiteralEditorControl(
    editable: EditableToken<Any>,
    context: Context,
    args: Bundle
) :
    FXTokenEditorView(editable, context, args) {
    init {
        root.styleClass.add("lisp-double-literal")
    }
}