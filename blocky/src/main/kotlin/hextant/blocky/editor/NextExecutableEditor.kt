/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.blocky.End
import hextant.blocky.Executable
import hextant.context.Context
import hextant.core.EditorView
import hextant.core.editor.AbstractEditor
import hextant.serial.Snapshot
import hextant.serial.reconstructEditor
import hextant.serial.snapshot
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import reaktive.value.ReactiveValue
import reaktive.value.binding.flatMap
import reaktive.value.binding.map
import reaktive.value.now
import reaktive.value.reactiveValue
import reaktive.value.reactiveVariable

class NextExecutableEditor(context: Context) :
    AbstractEditor<Executable, EditorView>(context) {
    private val next = reactiveVariable(null as ExecutableEditor<*>?)

    override val result: ReactiveValue<Executable> =
        next.flatMap { it?.result?.map { it ?: End } ?: reactiveValue(End) }

    fun setNext(next: ExecutableEditor<*>) {
        this.next.set(next)
    }

    fun clearNext() {
        next.set(null)
    }

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Snap : Snapshot<NextExecutableEditor>() {
        private var nxt: Snapshot<ExecutableEditor<*>>? = null

        override fun doRecord(original: NextExecutableEditor) {
            nxt = original.next.now?.snapshot(recordClass = true)
        }

        override fun reconstruct(original: NextExecutableEditor) {
            val e = nxt?.reconstructEditor(original.context)
            if (e is ExecutableEditor<*>) original.setNext(e)
        }

        override fun JsonObjectBuilder.encode() {
            put("next", nxt?.encode() ?: JsonNull)
        }

        override fun decode(element: JsonObject) {
            val next = element.getValue("next")
            nxt = if (next == JsonNull) null else decode<ExecutableEditor<*>>(next)
        }
    }
}