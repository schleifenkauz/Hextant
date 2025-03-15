/**
 *@author Nikolaus Knop
 */

package hextant.context

import hextant.core.Editor

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
     * @property content the snapshot of the copied editor.
     */
    data class OneEditor(val content: Editor<*>) : ClipboardContent()

    /**
     * The clipboard contains multiple editors.
     * @property editors the snapshots of the copied editors.
     */
    data class MultipleEditors(val editors: List<Editor<*>>) : ClipboardContent()
}