/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.core.view.TokenEditorView
import hextant.fx.EditorControl
import hextant.fx.HextantTextField
import hextant.lisp.editor.CharLiteralEditor
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class CharLiteralEditorControl(
    editor: CharLiteralEditor,
    args: Bundle
) : EditorControl<HBox>(editor, args), TokenEditorView {
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