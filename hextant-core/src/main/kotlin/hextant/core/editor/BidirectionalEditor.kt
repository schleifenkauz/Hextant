/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor

/**
 * An [Editor] which supports setting the [result] to a specific value and updating its state accordingly.
 */
interface BidirectionalEditor<R> : Editor<R> {
    /**
     * Sets the state of this editor such that its [result] will be the specified [value].
     */
    fun setResult(value: R)
}