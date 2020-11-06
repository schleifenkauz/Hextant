/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.view.ChoiceEditorView
import hextant.serial.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import reaktive.value.now
import reaktive.value.reactiveVariable
import validated.force
import validated.reaktive.ReactiveValidated
import validated.valid

/**
 * An [Editor] which supports choosing different items of type [C]
 */
abstract class ChoiceEditor<C : Any>(default: C, context: Context) : AbstractEditor<C, ChoiceEditorView<C>>(context) {
    private val selected = reactiveVariable(valid(default))

    override val result: ReactiveValidated<C>
        get() = selected

    /**
     * Select the given [choice]
     */
    fun select(choice: C) {
        selected.set(valid(choice))
        views { selected(choice) }
    }

    override fun createSnapshot(): Snapshot<*> = Snap<C>()

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    private class Snap<C : Any> : Snapshot<ChoiceEditor<C>>() {
        private lateinit var selected: C

        override fun doRecord(original: ChoiceEditor<C>) {
            selected = original.selected.now.force()
        }

        override fun reconstruct(original: ChoiceEditor<C>) {
            original.selected.now = valid(selected)
        }

        override fun JsonObjectBuilder.encode() {
            put("classOfSelected", selected.javaClass.name)
            val serializer = selected::class.serializer() as KSerializer<C>
            put("selected", Json.encodeToJsonElement(serializer, selected))
        }

        override fun decode(element: JsonObject) {
            val classOfSelected = element.getValue("classOfSelected").string
            val clazz = classOfSelected.loadClass().kotlin
            val serializer = clazz.serializer() as KSerializer<C>
            selected = Json.decodeFromJsonElement(serializer, element.getValue("selected"))
        }
    }
}