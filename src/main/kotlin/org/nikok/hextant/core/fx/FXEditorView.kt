/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.fx

import javafx.css.PseudoClass
import javafx.scene.Node
import org.nikok.hextant.EditorView

/**
 * An [EditorView] showing its content with a [Node]
*/
interface FXEditorView: EditorView {
    /**
     * The visual content of this [EditorView]
    */
    val node: Node

    override fun select(isSelected: Boolean) {
        node.pseudoClassStateChanged(PseudoClasses.SELECTED, isSelected)
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

}