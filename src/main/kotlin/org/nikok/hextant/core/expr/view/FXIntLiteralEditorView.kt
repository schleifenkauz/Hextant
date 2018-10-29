package org.nikok.hextant.core.expr.view

import javafx.scene.control.Control
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.editor.IntLiteralEditor
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.getEditor

class FXIntLiteralEditorView(
    editableInt: EditableIntLiteral, editorFactory: EditorFactory = HextantPlatform[Public, EditorFactory]
) : FXEditorView, HextantTextField(), IntLiteralEditorView {
    private val editor: IntLiteralEditor = editorFactory.getEditor(editableInt)

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
