/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.view.TokenEditorView
import hextant.fx.HextantTextField
import hextant.lisp.editor.CharLiteralEditor
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class CharLiteralEditorControl(
    editor: CharLiteralEditor,
    context: Context,
    args: Bundle
) : EditorControl<HBox>(editor, context, args), TokenEditorView {
    private val charTextField = HextantTextField()

    init {
        editor.addView(this)
        charTextField.textProperty().addListener { _, _, new ->
            editor.setText(new)
        }
        charTextField.styleClass.add("lisp-char-literal")
    }

    override fun createDefaultRoot(): HBox = HBox().apply {
        children.add(Label("'"))
        children.add(charTextField)
        children.add(Label("'"))
    }

    override fun displayText(newText: String) {
        charTextField.text = newText
    }

    override fun receiveFocus() {
        charTextField.requestFocus()
    }
}