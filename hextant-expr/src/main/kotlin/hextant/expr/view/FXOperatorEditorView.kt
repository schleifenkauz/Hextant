/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.expr.editor.OperatorEditor

class FXOperatorEditorView(
    editor: OperatorEditor,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editor, context, args) {
    init {
        root.styleClass.add("operator")
    }
}