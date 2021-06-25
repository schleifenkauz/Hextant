/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

class BlockEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: hextant.blocky.editor.BlockEditor, arguments: Bundle
) : ExecutableEditorControl<Pane>(editor, arguments) {
    init {
        configureArrowStart(editor.next)
        configureArrowTarget()
    }

    override fun createDefaultRoot(): Pane {
        val grab = Label("")
        val statements = context.createControl(editor.statements)
        val box = VBox(grab, statements)
        box.styleClass.add("block")
        return box
    }
}