package hextant.core.expr.view

import hextant.Context
import hextant.core.expr.editable.EditableIntLiteral

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, context: Context
) : FXTokenEditorView(editableInt, context) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
