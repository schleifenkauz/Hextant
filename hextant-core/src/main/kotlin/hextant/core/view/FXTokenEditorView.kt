/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.editable.EditableToken
import hextant.core.editor.TokenEditor
import hextant.fx.HextantTextField
import hextant.fx.smartSetText
import hextant.getEditor

open class FXTokenEditorView(
    editable: EditableToken<Any>,
    context: Context,
    args: Bundle
) : EditorControl<HextantTextField>(args), TextEditorView {
    private var updatingText = false

    final override fun createDefaultRoot() = HextantTextField().apply {
        textProperty().addListener { _, _, new ->
            if (!updatingText) {
                editor.setText(new)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val editor = context.getEditor(editable) as TokenEditor<*, TextEditorView>

    init {
        initialize(editable, editor, context)
        editor.addView(this)
    }

    final override fun displayText(t: String) {
        updatingText = true
        root.smartSetText(t)
        updatingText = false
    }
}