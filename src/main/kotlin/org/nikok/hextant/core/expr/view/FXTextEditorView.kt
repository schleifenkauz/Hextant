package org.nikok.hextant.core.expr.view

import org.nikok.hextant.core.editor.TextEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.core.view.TextEditorView
import org.nikok.reaktive.value.now

class FXTextEditorView(editable: EditableText) : FXEditorView, TextEditorView {
    override val node = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    private val editor = TextEditor(editable, this)

    init {
        displayText(editable.text.now)
        initSelection(editor)
        node.activateInspections(editable)
        node.activateContextMenu(editor)
    }

    override fun displayText(t: String) {
        node.text = t
    }
}