package hextant.core.view

import hextant.core.Editor
import hextant.core.EditorView

/**
 * An [EditorView] for list editors
 */
interface ListEditorView : EditorView {
    /**
     * Is called when a new child [editor] was added to the associated list editor at the specified [idx]
     */
    fun added(editor: Editor<*>, idx: Int)

    /**
     * Is called when the at the specified [idx] child editor was removed from the associated list editor
     */
    fun removed(idx: Int)

    /**
     * Is called when the associated list editor was previously not empty and is empty (has no child editors) now.
     */
    fun empty()

    /**
     * Is called when the associated list editor was previously empty [had no child editors] but is not empty now.
     */
    fun notEmpty()
}
