/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.core.EditorView

/**
 * A view that displays a single token editor
 */
interface TokenEditorView : EditorView {
    /**
     * Called when the text of the associated token editor changed
     */
    fun displayText(newText: String)
}