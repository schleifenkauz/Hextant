package org.nikok.hextant.core.expr.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.editor.TextEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.HextantTextField
import org.nikok.hextant.core.fx.smartSetText
import org.nikok.hextant.core.getEditor
import org.nikok.hextant.prop.get

open class FXTextEditorView(
    editable: EditableText,
    platform: HextantPlatform
) : EditorControl<HextantTextField>(), TextEditorView {
    final override fun createDefaultRoot() = HextantTextField().apply {
        textProperty().addListener { _, _, new -> editor.setText(new) }
    }

    private val editor: TextEditor = platform[EditorFactory].getEditor(editable)

    init {
        initialize(editable, editor, platform)
        editor.addView(this)
    }

    override fun displayText(t: String) {
        root.smartSetText(t)
    }
}