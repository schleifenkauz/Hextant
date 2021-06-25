/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.fx.HextantTextField
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

class BranchEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: hextant.blocky.editor.BranchEditor, arguments: Bundle
) :
    ExecutableEditorControl<Pane>(editor, arguments) {
    init {
        styleClass.add("branch")
        configureArrowTarget()
    }

    override fun createDefaultRoot(): Pane {
        val no = HextantTextField("No ")
        no.isEditable = false
        no.configureArrowStart(editor.no)
        val yes = HextantTextField("Yes")
        yes.configureArrowStart(editor.yes)
        yes.isEditable = false
        val cond = context.createControl(editor.condition)
        val content = HBox(no, cond, yes)
        return VBox(Label(""), content)
    }
}
