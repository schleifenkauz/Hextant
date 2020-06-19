/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor
import hextant.core.EditorView
import reaktive.value.binding.map
import validated.Validated
import validated.flatMap
import validated.reaktive.ReactiveValidated

internal class TransformedEditor<T, R>(
    internal val source: Editor<T>,
    transform: (T) -> Validated<R>
) : AbstractEditor<R, EditorView>(source.context) {
    override val result: ReactiveValidated<R> = source.result.map { it.flatMap(transform) }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot(original: TransformedEditor<*, *>) : EditorSnapshot<TransformedEditor<*, *>>(original) {
        private val snapshot = original.source.snapshot()

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: TransformedEditor<*, *>) {
            snapshot.reconstruct(editor.source)
        }
    }
}