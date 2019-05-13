/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.expr.editor.OperatorEditor

class FXOperatorEditorView(
    editor: OperatorEditor,
    args: Bundle
) : FXTokenEditorView(editor, args) {
    init {
        root.styleClass.add("operator")
    }
}