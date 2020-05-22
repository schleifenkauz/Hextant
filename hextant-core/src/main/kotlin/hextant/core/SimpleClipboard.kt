/**
 *@author Nikolaus Knop
 */

package hextant.core

import hextant.core.ClipboardContent.Empty

/**
 * A simple type of [Clipboard] that only stores the most recently copied content.
 */
class SimpleClipboard : Clipboard {
    private var current: ClipboardContent = Empty

    /**
     * Stores the given [content] inside this clipboard.
     */
    override fun copy(content: ClipboardContent) {
        current = content
    }

    /**
     * Returns the content most recently stored in the clipboard
     */
    override fun get(): ClipboardContent = current
}