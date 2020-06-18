/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.core.EditorView

/**
 * Displays a [hextant.core.editor.ValidatedTokenEditor]
 */
interface ValidatedTokenEditorView : EditorView {
    /**
     * Display the given [text].
     */
    fun displayText(text: String)

    /**
     * If [editable] is `true` make the view editable otherwise disable editing.
     */
    fun setEditable(editable: Boolean)
}