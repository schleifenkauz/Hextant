/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import reaktive.value.binding.map

internal class TransformedEditor<T : Any, R : Any>(
    internal val source: Editor<T>,
    transform: (T) -> CompileResult<R>
) : AbstractEditor<R, EditorView>(source.context) {
    override val result: EditorResult<R> = source.result.map { it.flatMap(transform) }
}