/**
 *@author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableJIdent
import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView

class JIdentEditorControl(
    editable: EditableJIdent,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        root.styleClass.add("identifier")
    }
}