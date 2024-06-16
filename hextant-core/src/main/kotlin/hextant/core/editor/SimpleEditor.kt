package hextant.core.editor

import hextant.context.Context
import hextant.serial.Snapshot
import hextant.serial.json
import hextant.serial.loadClass
import hextant.serial.string
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer
import reaktive.value.ReactiveValue
import reaktive.value.now
import reaktive.value.reactiveVariable

open class SimpleEditor<R : Any>(context: Context, result: R) : AbstractEditor<R, SimpleEditor.View<R>>(context) {
    private var _result = reactiveVariable(result)

    override val result: ReactiveValue<R> get() = _result

    fun setResult(result: R) {
        _result.set(result)
        notifyViews { displayResult(result) }
    }

    override fun viewAdded(view: View<R>) {
        view.displayResult(result.now)
    }

    override fun createSnapshot(): Snapshot<*> = Snap()

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    private class Snap : Snapshot<SimpleEditor<Any>>() {
        private lateinit var result: Any

        override fun doRecord(original: SimpleEditor<Any>) {
            result = original.result.now
        }

        override fun reconstructObject(original: SimpleEditor<Any>) {
            original.setResult(result)
        }

        override fun encode(builder: JsonObjectBuilder) {
            builder.put("resultClass", this.result.javaClass.name)
            val serializer = result::class.serializer() as KSerializer<Any>
            builder.put("result", json.encodeToJsonElement(serializer, result))
        }

        override fun decode(element: JsonObject) {
            val classOfSelected = element.getValue("resultClass").string
            val clazz = classOfSelected.loadClass().kotlin
            val serializer = clazz.serializer() as KSerializer<Any>
            result = json.decodeFromJsonElement(serializer, element.getValue("result"))
        }
    }

    interface View<R : Any> {
        fun displayResult(result: R)
    }
}