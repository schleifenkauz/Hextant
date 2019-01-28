/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.core.editable.EditableToken
import hextant.expr.view.FXTokenEditorView

class FXIntLiteralEditorView(
    editable: EditableToken<Any>,
    context: Context
) : FXTokenEditorView(editable, context) {
    init {
        styleClass.add("int-literal-editor")
    }
}