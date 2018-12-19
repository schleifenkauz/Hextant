package org.nikok.hextant.core.expr.view

import org.nikok.hextant.Context
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.editor.TextEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.HextantTextField
import org.nikok.hextant.core.fx.smartSetText
import org.nikok.hextant.getEditor

open class FXTextEditorView(
    editable: EditableText,
    context: Context
) : EditorControl<HextantTextField>(), TextEditorView {
    final override fun createDefaultRoot() = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    private val editor: TextEditor = context.getEditor(editable) as TextEditor

    init {
        initialize(editable, editor, context)
        editor.addView(this)
    }

    override fun displayText(t: String) {
        root.smartSetText(t)
    }
}