/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.Editor
import hextant.createView
import hextant.fx.setRoot
import hextant.serial.HextantFileManager
import javafx.scene.control.Control
import javafx.scene.control.Label

/**
 * A JavaFX [EditorPane] which simply changes its root when a new editor is shown.
 */
class SimpleEditorPane : EditorPane, Control() {
    private var currentEditor: Editor<*>? = null
    private val empty = Label("Please open a file")

    init {
        setRoot(empty)
    }

    override fun deleted(editor: Editor<*>) {
        if (editor == currentEditor) {
            currentEditor = null
            setRoot(empty)
        }
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