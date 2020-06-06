/**
 * @author Nikolaus Knop
 */

package hextant.core

import bundles.SimpleProperty

/**
 * A clipboard is used to store contents copied by the user.
 */
interface Clipboard {
    /**
     * Stores the given [content] inside this clipboard.
     */
    fun copy(content: ClipboardContent)

    /**
     * Returns the content most recently stored in the clipboard
     */
    fun get(): ClipboardContent

    companion object : SimpleProperty<Clipboard>("clipboard")
}