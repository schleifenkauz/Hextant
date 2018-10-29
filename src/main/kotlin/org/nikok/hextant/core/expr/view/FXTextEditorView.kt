package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.editor.TextEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.getEditor

class FXTextEditorView(
    editable: EditableText,
    editorFactory: EditorFactory = HextantPlatform[Public, EditorFactory]
) : FXEditorView, TextEditorView {
    override val node = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    private val editor: TextEditor = editorFactory.getEditor(editable)

    init {
        node.initSelection(editor)
        node.activateInspections(editable)
        node.activateContextMenu(editor)
        editor.addView(this)
    }

    override fun displayText(t: String) {
        node.smartSetText(t)
    }
}