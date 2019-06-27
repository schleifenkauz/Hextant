/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.Context
import hextant.Editor

/**
 * An [Editor] that supports copying it in a different context
 */
interface MovableEditor<R : Any> : Editor<R> {
    /**
     * Copy this editor such that the new editor has the given editor
     */
    fun copyFor(context: Context): Editor<R>
}