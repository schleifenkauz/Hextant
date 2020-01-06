/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.*
import hextant.fx.setRoot
import hextant.serial.HextantFileManager
import javafx.scene.control.Control
import javafx.scene.control.Label

class SimpleEditorPane : EditorPane, Control() {
    private var currentEditor: Editor<*>? = null

    init {
        setRoot(Label("Please open a file"))
    }

    override fun show(editor: Editor<*>) {
        if (editor == currentEditor) return
        currentEditor?.let {
            val manager = it.context[HextantFileManager]
            val file = manager.get(it)
            file.write()
        }
        currentEditor = editor
        val v = editor.context.createView(editor)
        setRoot(v)
        v.requestFocus()
    }
}