package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.editor.TextEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.getEditor
import org.nikok.hextant.get

class FXTextEditorView(
    editable: EditableText,
    platform: HextantPlatform
) : FXEditorView, TextEditorView {
    override val node = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    private val editor: TextEditor = platform[EditorFactory].getEditor(editable)

    init {
        node.initSelection(editor)
        node.activateInspections(editable, platform)
        node.activateContextMenu(editor, platform)
        editor.addView(this)
    }

    override fun displayText(t: String) {
        node.smartSetText(t)
    }
}