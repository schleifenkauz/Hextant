/**
 * @author Nikolaus Knop
 */

package hextant.core

import bundles.Bundle
import javafx.css.PseudoClass

/**
 * A graphical view of an [Editor]
 */
interface EditorView {
    /**
     * The arguments for this view, mutating those will change the ways it displays the content
     */
    val arguments: Bundle

    /**
     * The editor displayed by this [EditorView]
     */
    val target: Editor<*>

    /**
     * Visually deselect this [EditorView]
     */
    fun displaySelected(status: Boolean)

    /**
     * Select this [EditorView]
     * */
    fun select()

    fun toggleSelection()

    /**
     * Focus this [EditorView]
     */
    fun focus()
    /**
     * Change the state of the specified [pseudoClass].
     */
    fun changePseudoClassState(pseudoClass: PseudoClass, active: Boolean)
}