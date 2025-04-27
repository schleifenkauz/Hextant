/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.context.Context
import hextant.core.editor.Expander
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import hextant.serial.Root
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import reaktive.value.ReactiveValue
import kotlin.reflect.KClass

/**
 * An editor for results of type [R]
 */
@Serializable(with = Editor.Serializer::class)
interface Editor<out R> {
    val isInitialized: Boolean

    /**
     * A [reaktive.value.ReactiveValue] holding the result of compiling the content of the editor
     */
    val result: ReactiveValue<R>

    /**
     * The context of this editor
     */
    val context: Context

    /**
     * The parent of this Editor or `null` if this Editor is the root
     */
    val parent: Editor<*>?

    /**
     * @return the location of this editor relative its parent
     */
    val accessor: EditorAccessor?

    /**
     * The Expander that expanded this editor
     */
    val expander: Expander<*, *>?

    /**
     * The children of this editor
     */
    fun getChildren(): Collection<Editor<*>>

    /**
     * Is called when a new editor is created to initialize its variables to their default values.
     * */
    fun setupDefaultState() {}

    /**
     * Initialize this editor in the given [context]
     */
    fun initialize(
        context: Context,
        parent: Editor<*>? = null,
        accessor: EditorAccessor = Root,
        expander: Expander<*, *>? = null
    )

    /**
     * Return the child denoted by the given [accessor] or throw a [InvalidAccessorException] if there is no such child
     */
    fun getSubEditor(accessor: EditorAccessor): Editor<*>

    fun setAccessor(accessor: EditorAccessor)

    /**
     * Paste the given [editor] into this [Editor] if it is supported.
     * @return `true` only if pasting the given [editor] was successful.
     */
    fun paste(editor: Editor<*>): Boolean

    fun implCopy(): Editor<R> = throw UnsupportedOperationException("Copying ${javaClass.name} is not implemented")

    /**
     * Returns `true` only if this [Editor] supports copy/paste in principle.
     * The default implementation returns `false`.
     */
    fun supportsCopyPaste(): Boolean = false

    fun serialize(): JsonElement

    fun serialize(typeTag: Boolean): JsonElement

    fun deserialize(element: JsonElement)

    companion object {
        inline fun <reified E : Editor<*>> deserialize(json: JsonElement): E {
            val klass = E::class
            return deserialize(json, klass) as E
        }

        fun deserialize(json: JsonElement, klass: KClass<*>?): Editor<*> {
            if (klass == null) return deserializeWithTypeTag(json)
            val editor = klass.java.newInstance() as Editor<*>
            if (json is JsonObject && "_type" in json) error("Unexpected type tag in $json")
            editor.deserialize(json)
            return editor
        }

        fun deserializeWithTypeTag(json: JsonElement): Editor<*> {
            if (json !is JsonObject) error("No type tag found on $json")
            val type = json["_type"]?.jsonPrimitive?.content ?: error("No type tag found on $json")
            val editor = Class.forName(type).newInstance() as Editor<*>
            editor.deserialize(json["_content"] ?: json)
            return editor
        }
    }

    object Serializer : KSerializer<Editor<*>> {
        override val descriptor: SerialDescriptor = serialDescriptor<JsonElement>()

        override fun serialize(encoder: Encoder, value: Editor<*>) {
            encoder.encodeSerializableValue(kotlinx.serialization.serializer(), value.serialize())
        }

        override fun deserialize(decoder: Decoder): Editor<*> {
            val json: JsonElement = decoder.decodeSerializableValue(kotlinx.serialization.serializer())
            return deserialize(json) as Editor<*>
        }
    }
}