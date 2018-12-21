/**
 * @author Nikolaus Knop
 */

package hextant.core.fx

import hextant.EditorView
import javafx.application.Platform
import javafx.css.PseudoClass
import javafx.scene.Node

/**
 * An [EditorView] showing its content with a [Node]
 */
interface FXEditorView : EditorView {
    /**
     * The visual content of this [EditorView]
     */
    val node: Node

    override fun select(isSelected: Boolean) {
        if (isSelected) {
            println("select $this, with node $node")
            node.style = "-fx-background-color: #292929"
        } else {
            println("deselect $this, with node $node")
            node.style = null
        }
        /*node.pseudoClassStateChanged(PseudoClasses.SELECTED, isSelected)*/
    }

    override fun error(isError: Boolean) {
        node.pseudoClassStateChanged(PseudoClasses.ERROR, isError)
    }

    override fun focus() {
        node.requestFocus()
        node.pseudoClassStateChanged(REQUESTED_FOCUS, false)
    }

    private companion object {
        val REQUESTED_FOCUS: PseudoClass = PseudoClass.getPseudoClass("requested-focus")
    }

    override fun onGuiThread(action: () -> Unit) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(action)
        } else action()
    }
}