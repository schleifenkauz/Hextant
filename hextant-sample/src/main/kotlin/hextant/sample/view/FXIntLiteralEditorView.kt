/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import bundles.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.sample.editor.IntLiteralEditor

class FXIntLiteralEditorView(
    editable: IntLiteralEditor,
    args: Bundle
) : FXTokenEditorView(editable, args) {
    init {
        styleClass.add("int-literal-editor")
    }
}