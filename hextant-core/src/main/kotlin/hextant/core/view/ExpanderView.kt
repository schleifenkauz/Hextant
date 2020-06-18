/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.core.Editor
import hextant.core.EditorView

/**
 * A view that displays an Expander.
 * @see [hextant.core.editor.Expander] for more information about the expander model
 */
interface ExpanderView : EditorView {
    /**
     * Is called when the associated expander was expanded to the given [editor]
     */
    fun expanded(editor: Editor<*>)

    /**
     * Is called when the associated expander was reset
     */
    fun reset()

    /**
     * Is called when the associated expanders text was set to the given new text
     */
    fun displayText(text: String)
}