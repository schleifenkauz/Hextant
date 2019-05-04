package hextant.expr.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.expr.editor.IntLiteralEditor

class FXIntLiteralEditorView(
    intEditor: IntLiteralEditor, context: Context, args: Bundle
) : FXTokenEditorView(intEditor, context, args) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
