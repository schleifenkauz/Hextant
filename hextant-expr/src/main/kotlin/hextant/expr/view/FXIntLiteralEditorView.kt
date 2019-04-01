package hextant.expr.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.view.FXTokenEditorView
import hextant.expr.editable.EditableIntLiteral

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, context: Context, args: Bundle
) : FXTokenEditorView(editableInt, context, args) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
