/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import hextant.Editor
import hextant.base.EditorControl
import hextant.blocky.editor.*
import hextant.bundle.Bundle
import hextant.createView
import javafx.scene.Cursor
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import org.controlsfx.control.action.Action
import org.controlsfx.control.action.ActionUtils.createContextMenu

class ProgramEditorControl(editor: ProgramEditor, args: Bundle) : ProgramEditorView,
                                                                  EditorControl<Pane>(editor, args) {
    init {
        setPrefSize(1000.0, 1000.0)
        root.styleClass.add("program")
        contextMenu = createContextMenu(listOf(
            Action("New Block") { editor.addComponent(BlockEditor(context)) },
            Action("New Branch") { editor.addComponent(BranchEditor(context)) }
        ))
        editor.addView(this)
    }

    override fun createDefaultRoot(): AnchorPane = AnchorPane()

    override fun addedComponent(idx: Int, comp: Editor<*>) {
        val view = context.createView(comp)
        root.children.add(view)
        var startX = 0.0
        var startY = 0.0
        view.setOnMousePressed { ev ->
            startX = view.layoutX - ev.sceneX
            startY = view.layoutY - ev.sceneY
            view.cursor = Cursor.CLOSED_HAND
        }
        view.setOnMouseDragged { ev ->
            view.layoutX = ev.sceneX + startX
            view.layoutY = ev.sceneY + startY
        }
        view.setOnMouseReleased { view.cursor = Cursor.HAND }
    }

    override fun removedComponent(idx: Int) {
        root.children.removeAt(idx)
    }
}