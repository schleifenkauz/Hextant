package hextant.expr.view

import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.expr.editor.IntLiteralEditor

class FXIntLiteralEditorView(
    intEditor: IntLiteralEditor, args: Bundle
) : FXTokenEditorView(intEditor, args) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
