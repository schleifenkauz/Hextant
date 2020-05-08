/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import reaktive.value.binding.map

internal class TransformedEditor<T : Any, R : Any>(
    internal val source: Editor<T>,
    transform: (T) -> CompileResult<R>
) : AbstractEditor<R, EditorView>(source.context) {
    override val result: EditorResult<R> = source.result.map { it.flatMap(transform) }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot(original: TransformedEditor<*, *>) : EditorSnapshot<TransformedEditor<*, *>>(original) {
        private val snapshot = original.source.createSnapshot()

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: TransformedEditor<*, *>) {
            snapshot as EditorSnapshot<Editor<*>>
            snapshot.reconstruct(editor.source)
        }
    }
}