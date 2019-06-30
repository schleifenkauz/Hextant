/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.createView
import hextant.expr.editor.OperatorApplicationEditor
import hextant.fx.OperatorLabel
import javafx.scene.layout.HBox

class FXOperatorApplicationEditorView(
    editor: OperatorApplicationEditor,
    args: Bundle
) : EditorControl<HBox>(editor, args) {
    private val op1View: EditorControl<*> = context.createView(editor.operand1)

    private val operatorView: EditorControl<*> = context.createView(editor.operator)

    private val op2View: EditorControl<*> = context.createView(editor.operand2)

    override fun createDefaultRoot(): HBox {
        val openingParen = parenLabel("(")
        val closingParen = parenLabel(")")
        return HBox(openingParen, op1View, operatorView, op2View, closingParen)
    }

    init {
        defineChildren(op1View, operatorView, op2View)
        editor.addView(this)
    }

    private fun parenLabel(paren: String) = OperatorLabel(paren).apply {
        setOnMouseClicked {
            select()
        }
    }

    override fun receiveFocus() {
        op1View.receiveFocus()
    }
}