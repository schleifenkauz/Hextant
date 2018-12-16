package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.expr.editable.EditableIntLiteral

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, platform: HextantPlatform
) : FXTokenEditorView(editableInt, platform) {
    init {
        root.styleClass.add("decimal-editor")
    }
}
