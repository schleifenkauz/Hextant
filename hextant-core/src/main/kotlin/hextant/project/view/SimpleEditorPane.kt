/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.createView
import hextant.fx.setRoot
import javafx.scene.control.Control
import javafx.scene.control.Label

class SimpleEditorPane : EditorPane, Control() {
    init {
        setRoot(Label("Please open a file"))
    }

    override fun show(editor: Editor<*>) {
        setRoot(editor.context.createView(editor))
    }
}