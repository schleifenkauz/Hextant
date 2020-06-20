/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import bundles.Bundle
import hextant.context.createView
import hextant.core.view.EditorControl
import hextant.lisp.LispEditorTest.Companion.editorParentRegion
import hextant.lisp.editor.ApplyEditor
import javafx.beans.Observable
import javafx.scene.layout.*

@Suppress("RedundantLambdaArrow")
class ApplyEditorControl(
    editor: ApplyEditor,
    args: Bundle
) : EditorControl<Pane>(editor, args) {
    private val exprsView = context.createView(editor.editableExpressions)

    init {
        val maxWidth = context[editorParentRegion].widthProperty()
        //If _: Observable is removed, doesn't compile
        exprsView.widthProperty().addListener { _: Observable -> updateLayout() }
        maxWidth.addListener { _: Observable -> updateLayout() }
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