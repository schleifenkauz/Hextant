/**
 *@author Nikolaus Knop
 */

package hextant.expr.view

import hextant.Context
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.createView
import hextant.expr.editor.OperatorApplicationEditor
import hextant.fx.OperatorLabel
import hextant.fx.isControlDown
import javafx.scene.layout.HBox

class FXOperatorApplicationEditorView(
    editor: OperatorApplicationEditor,
    context: Context,
    args: Bundle
) : EditorControl<HBox>(editor, context, args) {
    private val op1View: EditorControl<*> = context.createView(editor.editableOp1)

    private val operatorView: EditorControl<*> = context.createView(editor.operatorEditor)

    private val op2View: EditorControl<*> = context.createView(editor.editableOp2)

    override fun createDefaultRoot(): HBox {
        val openingParen = parenLabel("(").apply {
            setOnMouseClicked {
                select()
            }
        }
        val closingParen = parenLabel(")").apply {
            setOnMouseClicked {
                select()
            }
        }
        return HBox(openingParen, op1View, operatorView, op2View, closingParen)
    }

    init {
        defineChildren(op1View, operatorView, op2View)
        editor.addView(this)
    }

    private fun parenLabel(paren: String) = OperatorLabel(paren).apply {
        setOnMouseClicked {
            if (isControlDown) {
                toggleSelection()
            } else {
                select()
            }
        }
    }

    override fun receiveFocus() {
        op1View.receiveFocus()
    }
}