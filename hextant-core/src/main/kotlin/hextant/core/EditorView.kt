/**
 * @author Nikolaus Knop
 */

package hextant.core

import bundles.Bundle
import hextant.serial.Snapshot

/**
 * A graphical view of an [Editor]
 */
interface EditorView {
    /**
     * The arguments for this view, mutating those will change the ways it displays the content
     */
    val arguments: Bundle

    /**
     * The object viewed by this [EditorView]
     */
    val target: Any

    /**
     * Visually deselect this [EditorView]
     */
    fun deselect()

    /**
     * Focus this [EditorView]
     */
    fun focus()

    /**
     * Create a snapshot of the current state of this [EditorView].
     *
     * The snapshot should only include layout and style information.
     */
    fun createSnapshot(): Snapshot<*>
}