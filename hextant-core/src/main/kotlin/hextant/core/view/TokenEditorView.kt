/**
 * @author Nikolaus Knop
 */

package hextant.core.view

/**
 * A view that displays a single token editor
 */
interface TokenEditorView {
    /**
     * Called when the text of the associated token editor changed
     */
    fun displayText(newText: String)
}