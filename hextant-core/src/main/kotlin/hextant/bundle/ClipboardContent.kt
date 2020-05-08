/**
 *@author Nikolaus Knop
 */

package hextant.bundle

import hextant.base.EditorSnapshot

/**
 * Represent the content of the clipboard
 */
sealed class ClipboardContent {
    /**
     * An empty clipboard, no contents have been copied yet.
     */
    object Empty : ClipboardContent()

    /**
     * The clipboard contains one editor.
     */
    data class OneEditor(val snapshot: EditorSnapshot<*>) : ClipboardContent()

    /**
     * The clipboard contains multiple editors.
     */
    data class MultipleEditors(val snapshots: List<EditorSnapshot<*>>) : ClipboardContent()
}