/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.bundle.Bundle

/**
 * A graphical view of an [Editor]
 * Acts like the View in the MVC pattern
 */
interface EditorView {
    /**
     * The arguments for this view, mutating those will change the ways it displays the content
     */
    val arguments: Bundle

    /**
     * Select this control
     */
    fun select(isSelected: Boolean)

    /**
     * Causes this view to somehow show that the associated editor has an error if [isError] is `true`.
     * Otherwise it stops displaying an error
     */
    fun error(isError: Boolean)

    /**
     * Causes this view to somehow show that the associated editor has a warning if [warn] is `true`.
     * Otherwise it stops displaying an error
     */
    fun warn(warn: Boolean)

    /**
     * Request focus for this [EditorView]
     * * In contrast to [receiveFocus] this method should really focus **this** view and not one of its children
     */
    fun focus()

    /**
     * Is called when this [EditorView] receives focus
     * * In case of parent views this method can also focus one of its children
     */
    fun receiveFocus()

    /**
     * Invoke that specified [action] on the gui thread of this [EditorView]
     */
    fun onGuiThread(action: () -> Unit) {
        action()
    }
}