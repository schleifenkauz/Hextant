/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.BidirectionalEditor
import hextant.Context

/**
 * A bidirectional token editor
 */
abstract class BidirectionalTokenEditor<R>(context: Context, text: String) :
    TokenEditor<R>(context, text), BidirectionalEditor<R> {
    override fun setResult(value: R) {
        setText(display(value))
    }

    /**
     * Convert the given [value] to a string.
     * The default implementation just uses [toString].
     */
    protected open fun display(value: R): String = value.toString()
}