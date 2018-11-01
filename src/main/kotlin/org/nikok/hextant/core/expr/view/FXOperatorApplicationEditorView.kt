/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import javafx.scene.layout.HBox
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.*
import org.nikok.hextant.core.expr.editable.EditableOperatorApplication
import org.nikok.hextant.core.expr.editor.OperatorApplicationEditor
import org.nikok.hextant.core.fx.*
import org.nikok.hextant.get

class FXOperatorApplicationEditorView(
    editable: EditableOperatorApplication,
    platform: HextantPlatform
) : FXEditorView, HBox() {
    private val op1View: FXEditorView

    private val operatorView: FXEditorView

    private val op2View: FXEditorView

    private val editorFactory = platform[EditorFactory]

    private val editor: OperatorApplicationEditor = editorFactory.getEditor(editable)

    init {
        val views = platform[EditorViewFactory]
        op1View = views.getFXView(editable.editableOp1)
        operatorView = views.getFXView(editable.editableOperator)
        op2View = views.getFXView(editable.editableOp2)
        val openingParen = parenLabel("(")
        val closingParen = parenLabel(")")
        children.addAll(openingParen, op1View.node, operatorView.node, op2View.node, closingParen)
        activateInspections(editable, platform)
        activateContextMenu(editor, platform)
        activateSelectionExtension(editor)
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

    override val node get() = this
}