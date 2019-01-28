package hextant.expr.view

import hextant.Context
import hextant.expr.editable.EditableIntLiteral

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, context: Context
) : FXTokenEditorView(editableInt, context) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
