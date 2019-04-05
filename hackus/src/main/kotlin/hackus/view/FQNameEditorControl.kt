/**
 *@author Nikolaus Knop
 */

package hackus.view

import hackus.editable.EditableFQName
import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView

class FQNameEditorControl(
    editable: EditableFQName,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        styleClass.add("fq-name")
    }
}