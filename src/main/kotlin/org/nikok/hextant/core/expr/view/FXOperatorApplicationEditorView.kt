/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import javafx.scene.layout.HBox
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.*
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.expr.editable.EditableOperatorApplication
import org.nikok.hextant.core.expr.editor.OperatorApplicationEditor
import org.nikok.hextant.core.fx.OperatorLabel
import org.nikok.hextant.core.fx.isControlDown
import org.nikok.hextant.prop.get

class FXOperatorApplicationEditorView(
    editable: EditableOperatorApplication,
    platform: HextantPlatform
) : EditorControl<HBox>() {
    private val op1View: EditorControl<*>

    private val operatorView: EditorControl<*>

    private val op2View: EditorControl<*>

    private val editorFactory = platform[EditorFactory]

    private val editor: OperatorApplicationEditor = editorFactory.getEditor(editable)

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
        val views = platform[EditorViewFactory]
        op1View = views.getFXView(editable.editableOp1)
        operatorView = views.getFXView(editable.editableOperator)
        op2View = views.getFXView(editable.editableOp2)
        initialize(editable, editor, platform)
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