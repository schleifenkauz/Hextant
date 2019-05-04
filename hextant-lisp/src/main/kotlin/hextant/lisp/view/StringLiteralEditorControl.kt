/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.view.TokenEditorView
import hextant.fx.HextantTextField
import hextant.fx.keyword
import hextant.lisp.editor.StringLiteralEditor
import javafx.scene.layout.HBox

class StringLiteralEditorControl(
    editor: StringLiteralEditor,
    context: Context,
    args: Bundle
) : TokenEditorView, EditorControl<HBox>(editor, context, args) {
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