/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.view

import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.expr.editable.EditableOperatorApplication
import org.nikok.hextant.core.expr.editor.OperatorApplicationEditor
import org.nikok.hextant.core.fx.*

class FXOperatorApplicationEditorView(editable: EditableOperatorApplication) : FXEditorView, HBox() {
    private val op1View: FXEditorView

    private val operatorView: FXEditorView

    private val op2View: FXEditorView

    init {
        val views = HextantPlatform[Public, EditorViewFactory]
        op1View = views.getFXView(editable.editableOp1)
        operatorView = views.getFXView(editable.editableOperator)
        op2View = views.getFXView(editable.editableOp2)
        children.addAll(Label("("), op1View.node, operatorView.node, op2View.node, Label(")"))
        val editor = OperatorApplicationEditor(editable, this)
        activateInspections(editable)
        activateContextMenu(editor)
    }

    override fun requestFocus() {
        op1View.focus()
    }

    override val node get() = this
}