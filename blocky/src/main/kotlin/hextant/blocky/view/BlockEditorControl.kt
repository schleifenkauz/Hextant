/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.BlockEditor
import hextant.createView
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

class BlockEditorControl(private val editor: BlockEditor, args: Bundle) : ExecutableEditorControl<Pane>(editor, args) {
    init {
        configureArrowStart(editor.next)
        configureArrowTarget()
    }

    override fun createDefaultRoot(): Pane {
        val grab = Label("")
        val statements = context.createView(editor.statements)
        val box = VBox(grab, statements)
        box.styleClass.add("block")
        return box
    }
}