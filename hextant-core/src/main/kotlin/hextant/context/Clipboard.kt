/**
 * @author Nikolaus Knop
 */

package hextant.context

import bundles.PublicProperty
import bundles.property

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

    companion object : PublicProperty<Clipboard> by property("clipboard")
}