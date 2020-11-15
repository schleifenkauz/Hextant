/**
 * @author Nikolaus Knop
 */

package hextant.core

import bundles.Bundle
import hextant.serial.SnapshotAware

/**
 * A graphical view of an [Editor]
 */
interface EditorView : SnapshotAware {
    /**
     * The arguments for this view, mutating those will change the ways it displays the content
     */
    val arguments: Bundle

    /**
     * The editor displayed by this [EditorView]
     */
    val target: Editor<*>

    /**
     * Visually deselect this [EditorView]
     */
    fun deselect()

    /**
     * Focus this [EditorView]
     */
    fun focus()
}