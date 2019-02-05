/**
 * @author Nikolaus Knop
 */

package hextant

/**
 * A graphical view of an [Editor]
 * Acts like the View in the MVC pattern
 */
interface EditorView {
    /**
     * Select this control
     */
    fun select(isSelected: Boolean)

    /**
     * Causes this view to somehow show that the associated editor has an error
     */
    fun error(isError: Boolean)

    /**
     * Request focus for this [EditorView]
     */
    fun focus()

    /**
     * Invoke that specified [action] on the gui thread of this [EditorView]
     */
    fun onGuiThread(action: () -> Unit) {
        action()
    }
}