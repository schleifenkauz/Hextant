/**
 *@author Nikolaus Knop
 */

package hackus.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.editable.EditableToken
import hextant.core.view.FXTokenEditorView

class JIdentEditorContol(
    editable: EditableToken<Any>,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        root.styleClass.add("identifier")
    }
}