/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.view

import hextant.*
import hextant.core.base.EditorControl
import hextant.core.expr.editable.EditableOperatorApplication
import hextant.core.expr.editor.OperatorApplicationEditor
import hextant.core.fx.OperatorLabel
import hextant.core.fx.isControlDown
import javafx.scene.layout.HBox

class FXOperatorApplicationEditorView(
    editable: EditableOperatorApplication,
    context: Context
) : EditorControl<HBox>() {
    private val op1View: EditorControl<*>

    private val operatorView: EditorControl<*>

    private val op2View: EditorControl<*>

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
        op1View = context.createView(editable.editableOp1)
        operatorView = context.createView(editable.editableOperator)
        op2View = context.createView(editable.editableOp2)
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

    override fun requestFocus() {
        op1View.focus()
    }
}