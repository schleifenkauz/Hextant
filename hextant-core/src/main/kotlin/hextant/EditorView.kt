/**
 * @author Nikolaus Knop
 */

package hextant

import bundles.Bundle

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
}