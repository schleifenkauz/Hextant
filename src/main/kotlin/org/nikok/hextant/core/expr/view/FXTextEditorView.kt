package org.nikok.hextant.core.expr.view

import org.nikok.hextant.core.editor.TextEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.fx.HextantTextField
import org.nikok.hextant.core.view.TextEditorView
import org.nikok.reaktive.value.now

class FXTextEditorView(editable: EditableText) : FXEditorView, TextEditorView {
    override val node = HextantTextField().apply {
        textProperty().addListener { _, _, new -> controller.setText(new) }
    }

    private val controller = TextEditor(editable, this)

    init {
        displayText(editable.text.now)
    }

    override fun displayText(t: String) {
        node.text = t
    }
}