/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.context.createControl
import hextant.core.Editor
import hextant.fx.setRoot
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
            val file = it.file ?: error("editor has no file")
            file.write()
        }
        currentEditor = editor
        val v = editor.context.createControl(editor)
        setRoot(v)
        v.requestFocus()
    }
}