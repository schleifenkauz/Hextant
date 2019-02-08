package hextant.core.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.editable.EditableText
import hextant.core.editor.TextEditor
import hextant.fx.HextantTextField
import hextant.fx.smartSetText
import hextant.getEditor
import reaktive.event.subscribe

open class FXTextEditorView(
    editable: EditableText,
    context: Context,
    args: Bundle
) : EditorControl<HextantTextField>(args), TextEditorView {
    final override fun createDefaultRoot() = HextantTextField().apply {
        userUpdatedText.subscribe { new -> editor.setText(new) }
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