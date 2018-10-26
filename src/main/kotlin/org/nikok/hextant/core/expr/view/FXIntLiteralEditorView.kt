package org.nikok.hextant.core.expr.view

import javafx.scene.control.Control
import org.nikok.hextant.core.expr.editable.*
import org.nikok.hextant.core.expr.editor.IntEditor
import org.nikok.hextant.core.fx.*
import org.nikok.reaktive.value.now

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral
) : FXEditorView, HextantTextField(), IntLiteralEditorView {
    private val editor = IntEditor(editableInt, this)

    init {
        activateContextMenu(editableInt)
        activateInspections(editableInt)
        initSelection(editor)
        styleClass.add(0, "decimal-editor")
        textProperty().addListener { _, _, new -> editor.setValue(new) }
        text = editableInt.text.now
    }

    override val node: Control
        get() = this

    override fun textChanged(new: String) {
        text = new
    }
}
