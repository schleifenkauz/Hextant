/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

/**
 * A graphical view of an [Editor]
 * Acts like the View in the MVC pattern
 */
interface EditorView {
    /**
     * Select this control
    */
    fun select(isSelected: Boolean)

    fun error(isError: Boolean)

    fun focus()

    fun onGuiThread(action: () -> Unit)
}