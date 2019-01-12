/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.base.EditorControl
import hextant.core.expr.view.TextEditorView
import hextant.getEditor
import hextant.lisp.editable.EditableCharLiteral
import hextant.lisp.editor.CharLiteralEditor
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox

class CharLiteralEditorControl(
    editable: EditableCharLiteral,
    context: Context
) : EditorControl<HBox>(), TextEditorView {
    private val charTextField = TextField()

    init {
        val editor = context.getEditor(editable) as CharLiteralEditor
        editor.addView(this)
        charTextField.textProperty().addListener { _, _, new ->
            editor.setText(new)
        }
        styleClass.add("char-literal")
        initialize(editable, editor, context)
    }

    override fun createDefaultRoot(): HBox = HBox().apply {
        children.add(Label("'"))
        children.add(charTextField)
        children.add(Label("'"))
    }

    override fun displayText(t: String) {
        charTextField.text = t
    }
}