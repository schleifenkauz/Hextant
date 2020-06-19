/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.core.view.EditorControl
import hextant.core.view.TokenEditorView
import hextant.fx.HextantTextField
import hextant.fx.keyword
import hextant.lisp.editor.StringLiteralEditor
import javafx.scene.layout.HBox

class StringLiteralEditorControl(
    editor: StringLiteralEditor,
    args: Bundle
) : TokenEditorView, EditorControl<HBox>(editor, args) {
    private val textField = HextantTextField()

    private val layout = HBox()

    init {
        textField.styleClass.add("lisp-string-literal")
        with(layout.children) {
            add(keyword("\""))
            add(textField)
            add(keyword("\""))
        }
    }

    override fun displayText(newText: String) {
        textField.text = newText
    }

    override fun createDefaultRoot(): HBox = layout

    override fun receiveFocus() {
        textField.requestFocus()
    }
}