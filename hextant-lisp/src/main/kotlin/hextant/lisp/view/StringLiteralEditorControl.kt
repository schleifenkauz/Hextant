/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.base.EditorControl
import hextant.core.expr.view.TextEditorView
import hextant.fx.keyword
import hextant.getEditor
import hextant.lisp.editable.EditableStringLiteral
import javafx.scene.control.TextField
import javafx.scene.layout.HBox

class StringLiteralEditorControl(editable: EditableStringLiteral, context: Context) :
    TextEditorView, EditorControl<HBox>() {
    private val textField = TextField()

    private val layout = HBox()

    init {
        styleClass.add("string-literal")
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