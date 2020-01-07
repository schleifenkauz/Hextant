/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.EditorView

interface FilteredTokenEditorView : EditorView {
    fun beginChange()

    fun displayText(text: String)

    fun endChange()
}