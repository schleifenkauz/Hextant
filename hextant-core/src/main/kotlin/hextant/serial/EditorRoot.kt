@file:OptIn(ExperimentalSerializationApi::class)

package hextant.serial

import hextant.context.Context
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.copyFor
import hextant.core.view.EditorControl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable(with = EditorRoot.Serializer::class)
class EditorRoot<E : Editor<*>> private constructor(val editor: E, private var controlArguments: JsonObject) {
    @Transient
    lateinit var control: EditorControl<*>
        private set

    constructor(editor: E): this(editor, JsonObject(emptyMap()))

    constructor(editor: E, context: Context) : this(editor, JsonObject(emptyMap())) {
        initialize(context)
    }

    constructor(editor: E, control: EditorControl<*>) : this(editor, control.exportJsonArgumentTree()) {
        this.control = control
    }

    fun initialize(context: Context) {
        editor.initialize(context, parent = null, accessor = Root)
        control = context.createControl(editor)
        control.initializeControl()
        control.importJsonArgumentTree(controlArguments)
    }

    fun clone(context: Context = editor.context): EditorRoot<E> {
        val editorCopy = editor.copyFor(context)
        val controlCopy = context.createControl(editorCopy)
        val argumentTree = control.exportJsonArgumentTree()
        controlCopy.importJsonArgumentTree(argumentTree)
        return EditorRoot(editorCopy, controlCopy)
    }

    object Serializer : KSerializer<EditorRoot<*>> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EditorRoot") {
                element<JsonElement>("editor")
                element<JsonElement>("controlArguments")
            }

        override fun serialize(encoder: Encoder, value: EditorRoot<*>) = encoder.encodeStructure(descriptor) {
            encodeSerializableElement(
                descriptor, 0, kotlinx.serialization.serializer(),
                value.editor.serialize(typeTag = true)
            )
            encodeSerializableElement(
                descriptor, 1, kotlinx.serialization.serializer(),
                value.control.exportJsonArgumentTree()
            )
        }

        override fun deserialize(decoder: Decoder): EditorRoot<*> = decoder.decodeStructure(descriptor) {
            lateinit var editorJson: JsonElement
            lateinit var controlArguments: JsonObject
            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> editorJson = decodeSerializableElement(descriptor, 0, kotlinx.serialization.serializer())
                    1 -> controlArguments = decodeSerializableElement(descriptor, 1, kotlinx.serialization.serializer())
                    else -> break
                }
            }
            val editor = Editor.deserializeWithTypeTag(editorJson)
            EditorRoot(editor, controlArguments)
        }
    }

    companion object {
        fun <E : Editor<*>> initialize(editor: E, context: Context): EditorRoot<E> {
            editor.setupDefaultState()
            val root = EditorRoot(editor, JsonObject(emptyMap()))
            root.initialize(context)
            return root
        }
    }
}