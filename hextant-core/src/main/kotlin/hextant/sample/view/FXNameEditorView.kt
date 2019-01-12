/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.core.expr.view.FXTokenEditorView
import hextant.sample.editable.EditableName

class FXNameEditorView(
    editable: EditableName,
    context: Context
) : FXTokenEditorView(editable, context) {
    init {
        styleClass.add("identifier-editor")
    }
}