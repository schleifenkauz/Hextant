/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import hextant.blocky.editor.BranchEditor
import hextant.bundle.Bundle
import hextant.createView
import hextant.fx.HextantTextField
import javafx.scene.control.Label
import javafx.scene.layout.*

class BranchEditorControl(private val editor: BranchEditor, args: Bundle) :
    ExecutableEditorControl<Pane>(editor, args) {
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
        val cond = context.createView(editor.condition)
        val content = HBox(no, cond, yes)
        return VBox(Label(""), content)
    }
}
