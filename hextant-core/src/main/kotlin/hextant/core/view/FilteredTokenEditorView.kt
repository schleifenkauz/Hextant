/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.EditorView

interface FilteredTokenEditorView : EditorView {
    fun displayText(text: String)

    fun setEditable(editable: Boolean)
}