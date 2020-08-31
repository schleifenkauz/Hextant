/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.view.TokenEditorView
import validated.*

/**
 * A [TokenEditor] which only compiles completions.
 */
abstract class CompletionTokenEditor<T : Any>(context: Context) : TokenEditor<T, TokenEditorView>(context) {
    private val t = getTypeArgument(CompletionTokenEditor::class, 0)

    override fun compile(item: Any): Validated<T> {
        @Suppress("UNCHECKED_CAST")
        return if (t.isInstance(item)) valid(item as T) else invalidComponent
    }
}