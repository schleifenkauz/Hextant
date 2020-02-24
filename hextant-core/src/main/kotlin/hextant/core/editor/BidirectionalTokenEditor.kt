/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.BidirectionalEditor
import hextant.Context
import hextant.core.view.TokenEditorView

/**
 * A bidirectional token editor
 */
abstract class BidirectionalTokenEditor<R : Any>(context: Context, text: String) :
    TokenEditor<R, TokenEditorView>(context, text), BidirectionalEditor<R> {
    override fun setResult(value: R) {
        setText(display(value))
    }

    /**
     * Convert the given [value] to a string.
     * The default implementation just uses [toString].
     */
    protected open fun display(value: R): String = value.toString()
}