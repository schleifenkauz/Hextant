/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.expr.editable.EditableOperatorApplication
import hextant.expr.editor.OperatorApplicationEditor
import hextant.fx.OperatorLabel
import hextant.fx.isControlDown
import javafx.scene.layout.HBox

class FXOperatorApplicationEditorView(
    editable: EditableOperatorApplication,
    context: Context,
    args: Bundle
) : EditorControl<HBox>(args) {
    private val op1View: EditorControl<*> = context.createView(editable.editableOp1)

    private val operatorView: EditorControl<*> = context.createView(editable.editableOperator)

    private val op2View: EditorControl<*> = context.createView(editable.editableOp2)

    private val editor: OperatorApplicationEditor = context.getEditor(editable) as OperatorApplicationEditor

    override fun createDefaultRoot(): HBox {
        val openingParen = parenLabel("(").apply {
            setOnMouseClicked {
                editor.select()
            }
        }
        val closingParen = parenLabel(")").apply {
            setOnMouseClicked {
                editor.select()
            }
        }
        return HBox(openingParen, op1View, operatorView, op2View, closingParen)
    }

    init {
        initialize(editable, editor, context)
        editor.addView(this)
    }

    private fun parenLabel(paren: String) = OperatorLabel(paren).apply {
        setOnMouseClicked {
            if (isControlDown) {
                editor.toggleSelection()
            } else {
                editor.select()
            }
        }
    }

    override fun receiveFocus() {
        op1View.receiveFocus()
    }
}