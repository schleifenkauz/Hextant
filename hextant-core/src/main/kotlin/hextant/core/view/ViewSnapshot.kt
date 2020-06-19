/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.core.EditorView
import hextant.core.Snapshot

/**
 * A [Snapshot] for [EditorControl]s
 */
interface ViewSnapshot<V : EditorView> {
    /**
     * Reconstruct the [EditorView] for the given [target] using the specified [arguments].
     */
    fun reconstruct(target: Any, arguments: Bundle): V

    /**
     * Reconstruct the given [EditorView] *inplace*.
     */
    fun reconstruct(view: V)
}