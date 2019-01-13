/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.CoreProperties.editorParentRegion
import hextant.fx.operator
import hextant.lisp.editable.EditableApply
import hextant.lisp.editor.ApplyEditor
import javafx.beans.Observable
import javafx.scene.control.Label
import javafx.scene.layout.*

class ApplyEditorControl(
    editable: EditableApply,
    private val context: Context
) : EditorControl<Pane>() {
    private val appliedView = context.createView(editable.editableApplied)

    private val argumentsView = context.createView(editable.editableArgs)

    init {
        val maxWidth = context[editorParentRegion].widthProperty()
        val updateLayout = { _: Observable -> updateLayout() }
        argumentsView.widthProperty().addListener(updateLayout)
        appliedView.widthProperty().addListener(updateLayout)
        maxWidth.addListener(updateLayout)
        val editor = context.getEditor(editable) as ApplyEditor
        initialize(editable, editor, context)
        root.styleClass.add("lisp-apply")
    }

    private fun updateLayout() {
        if (fitsHorizontally()) {
            if (root is HBox) root
            else root = createHorizontalLayout()
        } else {
            if (root is VBox) root
            else root = createVerticalLayout()
        }
    }

    private fun createVerticalLayout(): Pane = VBox().also {
        with(it.children) {
            add(HBox(operator("("), appliedView))
            add(HBox().apply {
                children.add(Label(""))
                children.add(argumentsView)
            })
        }
    }

    private fun createHorizontalLayout(): HBox = HBox().also {
        with(it.children) {
            add(operator("("))
            add(appliedView)
            add(argumentsView)
            add(operator(")"))
        }
    }

    override fun requestFocus() {
        appliedView.requestFocus()
    }

    private fun fitsHorizontally() = context[editorParentRegion].width >= argumentsView.width + appliedView.width

    override fun createDefaultRoot(): Pane =
        if (fitsHorizontally()) createHorizontalLayout() else createVerticalLayout()
}