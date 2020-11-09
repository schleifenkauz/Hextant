/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor
import hextant.core.EditorView
import hextant.serial.Snapshot
import hextant.serial.snapshot
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

internal class TransformedEditor<T, R>(
    internal val source: Editor<T>,
    transform: (T) -> R
) : AbstractEditor<R, EditorView>(source.context) {
    override val result: ReactiveValue<R> = source.result.map(transform)

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Snap : Snapshot<TransformedEditor<*, *>>() {
        private lateinit var snapshot: Snapshot<Editor<*>>

        override fun doRecord(original: TransformedEditor<*, *>) {
            snapshot = original.source.snapshot()
        }

        override fun reconstruct(original: TransformedEditor<*, *>) {
            snapshot.reconstruct(original.source)
        }

        override fun JsonObjectBuilder.encode() {
            put("source", snapshot.encode())
        }

        override fun decode(element: JsonObject) {
            snapshot = decode<Editor<*>>(element.getValue("source"))
        }
    }
}