package org.nikok.hextant.core.expr.view

import javafx.scene.control.Control
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.editor.IntLiteralEditor
import org.nikok.hextant.core.fx.*

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral
) : FXEditorView, HextantTextField(), IntLiteralEditorView {
    private val editor = IntLiteralEditor(editableInt)

    init {
        editor.addView(this)
        activateContextMenu(editor)
        activateInspections(editableInt)
        initSelection(editor)
        styleClass.add(0, "decimal-editor")
        textProperty().addListener { _, _, new -> editor.setValue(new) }
    }

    override val node: Control
        get() = this

    override fun textChanged(new: String) {
        smartSetText(new)
    }
}
