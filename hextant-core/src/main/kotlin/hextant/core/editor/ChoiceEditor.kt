/**
 *@author Nikolaus Knop
 */

@file:Suppress("DEPRECATION")

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.view.ChoiceEditorView
import hextant.serial.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer
import reaktive.value.ReactiveValue
import reaktive.value.binding.flatMap
import reaktive.value.now
import reaktive.value.reactiveVariable

/**
 * An [Editor] which supports choosing different items of type [C]
 */
abstract class ChoiceEditor<C : Any, R, E : Editor<R>>(context: Context, default: C) :
    AbstractEditor<R, ChoiceEditorView<C, E>>(context), ComboBoxSource<C> {
    private val _selected = reactiveVariable(default)
    private val _content = reactiveVariable(createEditor(default))

    val selected: ReactiveValue<C> get() = _selected
    val content: ReactiveValue<E> get() = _content

    override val result: ReactiveValue<R> = _content.flatMap { it.result }

    init {
        content.now.initParent(this)
        content.now.setAccessor(ChoiceEditorContent)
    }

    /**
     * Select the given [choice]
     */
    override fun select(choice: C) {
        if (choice == selected.now) return
        val editor = createEditor(choice)
        select(choice, editor)
    }

    fun select(choice: C, editor: E) {
        doSelect(choice, editor)
        views { selected(choice, editor) }
    }

    private fun doSelect(choice: C, editor: E) {
        _selected.set(choice)
        _content.set(editor)
        editor.initParent(this)
        editor.setAccessor(ChoiceEditorContent)
    }

    override fun toString(choice: C): String = choice.toString()

    override fun fromString(str: String): C? = null

    protected abstract fun createEditor(choice: C): E

    override fun createSnapshot(): Snapshot<*> = Snap()

    override fun viewAdded(view: ChoiceEditorView<C, E>) {
        view.selected(selected.now, content.now)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor is ChoiceEditorContent) {
            return content.now
        }
        return super.getSubEditor(accessor)
    }

    @Deprecated("Treat as internal")
    override fun initParent(parent: Editor<*>) {
        super.initParent(parent)
        content.now.initParent(parent)
    }

    @OptIn(InternalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    private class Snap : Snapshot<ChoiceEditor<*, *, *>>() {
        private lateinit var selected: Any
        private lateinit var content: Snapshot<Editor<*>>

        override fun doRecord(original: ChoiceEditor<*, *, *>) {
            selected = original.selected.now
            content = original.content.now.snapshot(recordClass = true)
        }

        override fun reconstructObject(original: ChoiceEditor<*, *, *>) {
            original as ChoiceEditor<Any, *, Editor<*>>
            val editor = content.reconstructEditor(original.context)
            original.doSelect(selected, editor)
        }

        override fun encode(builder: JsonObjectBuilder) {
            builder.put("classOfSelected", this.selected.javaClass.name)
            val serializer = selected::class.serializer() as KSerializer<Any>
            builder.put("selected", json.encodeToJsonElement(serializer, this.selected))
            builder.put("content", json.encodeToJsonElement(Serializer, this.content))
        }

        override fun decode(element: JsonObject) {
            val classOfSelected = element.getValue("classOfSelected").string
            val clazz = classOfSelected.loadClass().kotlin
            val serializer = clazz.serializer() as KSerializer<Any>
            selected = json.decodeFromJsonElement(serializer, element.getValue("selected"))
            content = json.decodeFromJsonElement(Serializer, element.getValue("content")) as Snapshot<Editor<*>>
        }
    }
}