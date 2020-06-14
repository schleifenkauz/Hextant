/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.EditorView

/**
 * Displays a [hextant.core.editor.FilteredTokenEditor]
 */
interface FilteredTokenEditorView : EditorView {
    /**
     * Display the given [text].
     */
    fun displayText(text: String)

    /**
     * If [editable] is `true` make the view editable otherwise disable editing.
     */
    fun setEditable(editable: Boolean)
}