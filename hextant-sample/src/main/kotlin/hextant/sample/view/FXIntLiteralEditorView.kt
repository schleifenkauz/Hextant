/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.sample.editor.IntLiteralEditor

class FXIntLiteralEditorView(
    editable: IntLiteralEditor,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        styleClass.add("int-literal-editor")
    }
}