/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CoreProperties.editorParentRegion
import hextant.lisp.editable.EditableApply
import hextant.lisp.editor.ApplyEditor
import javafx.beans.Observable
import javafx.scene.layout.*

class ApplyEditorControl(
    editable: EditableApply,
    private val context: Context,
    args: Bundle
) : EditorControl<Pane>(args) {
    private val exprsView = context.createView(editable.editableExpressions)

    init {
        val maxWidth = context[editorParentRegion].widthProperty()
        exprsView.widthProperty().addListener { _: Observable -> updateLayout() }
        maxWidth.addListener { _: Observable -> updateLayout() }
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
        TODO()
    }

    private fun createHorizontalLayout(): HBox = HBox().also {
        with(it.children) {
            //            add(operator("("))
            add(exprsView)
            //            add(operator(")"))
        }
    }

    override fun receiveFocus() {
        exprsView.receiveFocus()
    }

    private fun fitsHorizontally() = context[editorParentRegion].width >= exprsView.width

    override fun createDefaultRoot(): Pane =
        if (fitsHorizontally()) createHorizontalLayout() else createVerticalLayout()
}