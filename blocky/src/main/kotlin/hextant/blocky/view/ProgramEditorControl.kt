/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.BlockEditor
import hextant.blocky.editor.BranchEditor
import hextant.blocky.editor.EntryEditor
import hextant.blocky.editor.ProgramEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.EditorControlGroup
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.view.EditorControl
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import org.controlsfx.control.action.Action
import org.controlsfx.control.action.ActionUtils.createContextMenu

class ProgramEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: ProgramEditor, arguments: Bundle
) : ProgramEditorView, EditorControl<Pane>(editor, arguments) {
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
        val view = context.createControl(comp)
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