/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.core.view.TextEditorView
import hextant.fx.HextantTextField
import hextant.fx.keyword
import hextant.getEditor
import hextant.lisp.editable.EditableStringLiteral
import javafx.scene.layout.HBox

class StringLiteralEditorControl(
    editable: EditableStringLiteral,
    context: Context,
    args: Bundle
) :
    TextEditorView, EditorControl<HBox>(args) {
    private val textField = HextantTextField()

    private val layout = HBox()

    init {
        textField.styleClass.add("lisp-string-literal")
        with(layout.children) {
            add(keyword("\""))
            add(textField)
            add(keyword("\""))
        }
        val editor = context.getEditor(editable)
        initialize(editable, editor, context)
    }

    override fun displayText(t: String) {
        textField.text = t
    }

    override fun createDefaultRoot(): HBox = layout
}