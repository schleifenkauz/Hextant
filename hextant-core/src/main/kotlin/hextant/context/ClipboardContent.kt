/**
 *@author Nikolaus Knop
 */

package hextant.context

import hextant.core.Editor
import hextant.serial.Snapshot

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
     * @property snapshot the snapshot of the copied editor.
     */
    data class OneEditor(val snapshot: Snapshot<out Editor<*>>) : ClipboardContent()

    /**
     * The clipboard contains multiple editors.
     * @property snapshots the snapshots of the copied editors.
     */
    data class MultipleEditors(val snapshots: List<Snapshot<*>>) : ClipboardContent()
}