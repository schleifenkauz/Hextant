package org.nikok.hextant.core.expr.view

import javafx.scene.control.Control
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.editor.IntLiteralEditor
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.getEditor
import org.nikok.hextant.get

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, platform: HextantPlatform
) : FXEditorView, HextantTextField(), IntLiteralEditorView {
    private val editor: IntLiteralEditor = platform[EditorFactory].getEditor(editableInt)

    init {
        editor.addView(this)
        activateContextMenu(editor, platform)
        activateInspections(editableInt, platform)
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
