/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.sample.editor.IntOperatorEditor

class FXIntOperatorEditorView(
    editor: IntOperatorEditor,
    args: Bundle
) : FXTokenEditorView(editor, args) {
    init {
        styleClass.add("int-operator-editor")
    }
}