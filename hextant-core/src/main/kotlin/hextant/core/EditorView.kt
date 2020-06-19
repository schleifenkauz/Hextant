/**
 * @author Nikolaus Knop
 */

package hextant.core

import bundles.Bundle
import hextant.context.ViewGroup
import hextant.core.view.ViewSnapshot

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
     * The [ViewGroup] this editor view belongs to
     */
    val group: ViewGroup<*>

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
     * Information about the current state of editing is stored in [hextant.core.editor.EditorSnapshot]s.
     */
    fun createSnapshot(): ViewSnapshot<*>
}