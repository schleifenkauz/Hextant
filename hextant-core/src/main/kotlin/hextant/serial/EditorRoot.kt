@file:OptIn(ExperimentalSerializationApi::class)

package hextant.serial

import hextant.context.Context
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.copyFor
import hextant.core.view.EditorControl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
class EditorRoot<E : Editor<*>> private constructor(val editor: E, private var controlArguments: JsonElement) {
    @Transient
    private lateinit var control: EditorControl<*>

    constructor(editor: E, context: Context) : this(editor, JsonObject(emptyMap())) {
        initialize(context)
    }

    constructor(editor: E, control: EditorControl<*>) : this(editor, control.exportJsonArgumentTree()) {
        this.control = control
    }

    fun initialize(context: Context) {
        editor.initialize(context)
        control = context.createControl(editor)
        control.importJsonArgumentTree(controlArguments)
    }

    fun prepareSerialization() {
        controlArguments = control.exportJsonArgumentTree()
    }

    fun clone(context: Context): EditorRoot<E> {
        val editorCopy = editor.copyFor(context)
        val controlCopy = context.createControl(editorCopy)
        val argumentTree = control.exportJsonArgumentTree()
        controlCopy.importJsonArgumentTree(argumentTree)
        return EditorRoot(editorCopy, controlCopy)
    }

    companion object {
        fun <E : Editor<*>> create(editor: E): EditorRoot<E> {
            val control = editor.context.createControl(editor)
            return EditorRoot(editor, control)
        }
    }
}