/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.sample.editable.EditableName

class FXNameEditorView(
    editable: EditableName,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        styleClass.add("identifier-editor")
    }
}