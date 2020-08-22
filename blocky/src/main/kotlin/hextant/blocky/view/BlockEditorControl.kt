/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.blocky.editor.BlockEditor
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createView
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

class BlockEditorControl @ProvideImplementation(
    ControlFactory::class,
    BlockEditor::class
) constructor(private val editor: BlockEditor, args: Bundle) : ExecutableEditorControl<Pane>(editor, args) {
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