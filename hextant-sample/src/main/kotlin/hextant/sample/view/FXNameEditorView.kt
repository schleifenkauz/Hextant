/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.sample.editor.NameEditor

class FXNameEditorView(
    editable: NameEditor,
    args: Bundle
) : FXTokenEditorView(editable, args) {
    init {
        styleClass.add("identifier-editor")
    }
}