/**
 *@author Nikolaus Knop
 */

package hextant.blocky.view

import hextant.base.EditorControl
import hextant.blocky.editor.BranchEditor
import hextant.bundle.Bundle
import hextant.createView
import javafx.scene.control.Label
import javafx.scene.layout.HBox

class BranchEditorControl(private val editor: BranchEditor, args: Bundle) : EditorControl<HBox>(editor, args) {
    override fun createDefaultRoot(): HBox = HBox(
        Label("No"),
        context.createView(editor.condition),
        Label("Yes")
    )
}