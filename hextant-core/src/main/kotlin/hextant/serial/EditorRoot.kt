@file:OptIn(ExperimentalSerializationApi::class)

package hextant.serial

import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.copyFor
import hextant.core.view.EditorControl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer

@Serializable(with = EditorRoot.Serializer::class)
class EditorRoot<E : Editor<*>>(val editor: E, val control: EditorControl<*>) {
    init {
        editor.makeRoot()
    }

    fun clone(): EditorRoot<E> {
        val editorCopy = editor.copyFor(editor.context)
        val controlCopy = editor.context.createControl(editorCopy)
        control.snapshot().reconstructObject(controlCopy)
        return EditorRoot(editorCopy, controlCopy)
    }

    class Serializer(@Suppress("UNUSED_PARAMETER") serializer: KSerializer<Editor<*>>) : KSerializer<EditorRoot<*>> {
        override val descriptor: SerialDescriptor = serialDescriptor<JsonObject>()

        override fun serialize(encoder: Encoder, value: EditorRoot<*>) {
            val obj = buildJsonObject {
                put("editor", value.editor.snapshot(recordClass = true).encodeToJson())
                put("control", value.control.snapshot(recordClass = true).encodeToJson())
            }
            encoder.encodeSerializableValue(serializer(), obj)
        }

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(decoder: Decoder): EditorRoot<*> {
            val obj = decoder.decodeSerializableValue(serializer<JsonObject>())
            val editorSnapshot = Snapshot.decodeFromJson(obj.getValue("editor"))
            val editor = editorSnapshot.reconstruct(SnapshotAware.Serializer.reconstructionContext) as Editor<*>
            val controlSnapshot = Snapshot.decodeFromJson(obj.getValue("control")) as Snapshot<EditorControl<*>>
            val control = editor.context.createControl(editor)
            controlSnapshot.reconstructObject(control)
            return EditorRoot(editor, control)
        }
    }

    companion object {
        fun <E : Editor<*>> create(editor: E): EditorRoot<E> {
            val control = editor.context.createControl(editor)
            return EditorRoot(editor, control)
        }
    }

}