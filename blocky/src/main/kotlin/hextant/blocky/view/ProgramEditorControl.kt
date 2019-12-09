/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import hextant.*
import hextant.base.EditorControl
import hextant.blocky.editor.*
import hextant.bundle.Bundle
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import org.controlsfx.control.action.Action
import org.controlsfx.control.action.ActionUtils.createContextMenu

class ProgramEditorControl(private val editor: ProgramEditor, args: Bundle) : ProgramEditorView,
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

    override fun createDefaultRoot(): Pane = Pane()

    override fun addedComponent(idx: Int, comp: Editor<*>) {
        val view = context.createView(comp)
        root.children.add(view)
        var startX = 0.0
        var startY = 0.0
        view.setOnMousePressed { ev ->
            startX = view.layoutX - ev.sceneX
            startY = view.layoutY - ev.sceneY
            view.cursor = Cursor.CLOSED_HAND
            ev.consume()
        }
        view.setOnMouseDragged { ev ->
            view.layoutX = ev.sceneX + startX
            view.layoutY = ev.sceneY + startY
            ev.consume()
        }
        if (comp !is EntryEditor) {
            val remove = MenuItem("Remove")
            remove.setOnAction { editor.removeComponent(comp) }
            if (view.contextMenu == null) view.contextMenu = ContextMenu()
            view.contextMenu.items.add(remove)
        }
    }

    override fun removedComponent(comp: Editor<*>) {
        val view = context[EditorControlGroup].getViewOf(comp)
        root.children.remove(view)
    }

    fun displayArrow(from: Node, to: Node) {
        val line = Line()
        line.startXProperty().bind(from.layoutXProperty())
        line.startYProperty().bind(from.layoutYProperty())
        line.endXProperty().bind(to.layoutXProperty())
        line.endYProperty().bind(to.layoutYProperty())
        root.children.add(line)
    }
}