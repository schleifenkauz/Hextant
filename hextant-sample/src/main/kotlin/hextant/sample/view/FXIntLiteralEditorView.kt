/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.editable.EditableToken
import hextant.core.view.FXTokenEditorView

class FXIntLiteralEditorView(
    editable: EditableToken<Any>,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        styleClass.add("int-literal-editor")
    }
}