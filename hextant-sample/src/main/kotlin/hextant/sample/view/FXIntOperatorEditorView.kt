/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.sample.editor.IntOperatorEditor

class FXIntOperatorEditorView(
    editor: IntOperatorEditor,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editor, context, args) {
    init {
        styleClass.add("int-operator-editor")
    }
}