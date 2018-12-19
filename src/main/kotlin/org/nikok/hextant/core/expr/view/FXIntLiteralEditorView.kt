package org.nikok.hextant.core.expr.view

import org.nikok.hextant.Context
import org.nikok.hextant.core.expr.editable.EditableIntLiteral

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, context: Context
) : FXTokenEditorView(editableInt, context) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
