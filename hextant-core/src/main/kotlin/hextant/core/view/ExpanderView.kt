/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.Editor
import hextant.EditorView

interface ExpanderView : EditorView {
    fun expanded(editor: Editor<*>)

    fun reset()

    fun displayText(text: String)
}