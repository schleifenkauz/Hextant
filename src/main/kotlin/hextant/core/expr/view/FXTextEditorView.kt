package hextant.core.expr.view

import hextant.Context
import hextant.core.base.EditorControl
import hextant.core.editor.TextEditor
import hextant.core.expr.editable.EditableText
import hextant.core.fx.HextantTextField
import hextant.core.fx.smartSetText
import hextant.getEditor

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