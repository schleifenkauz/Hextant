/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor
import hextant.core.view.ChoiceEditorView
import hextant.serial.ChoiceEditorContent
import hextant.serial.EditorAccessor
import hextant.serial.JsonSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import reaktive.value.*
import reaktive.value.binding.flatMap
import kotlin.reflect.KClass

/**
 * An [Editor] which supports choosing different items of type [C]
 */
@Serializable
abstract class ChoiceEditor<C : Any, R, E : Editor<R>> :
    AbstractEditor<R, ChoiceEditorView<C, E>>(), ChoiceSource<C>, JsonSerializer<C> {
    private lateinit var _selected: ReactiveVariable<C>
    private lateinit var _content: ReactiveVariable<E>

    val selected: ReactiveValue<C> get() = _selected
    val content: ReactiveValue<E> get() = _content

    @Transient
    final override lateinit var result: ReactiveValue<R>
        private set

    fun selectInitial(choice: C) {
        check(!isInitialized) { "Already initialized" }
        _selected = reactiveVariable(choice)
        _content = reactiveVariable(createEditor(choice))
    }

    fun selectInitial(choice: C, editor: E) {
        check(!isInitialized) { "Already initialized" }
        _selected = reactiveVariable(choice)
        _content = reactiveVariable(editor)
    }

    override fun doInitialize() {
        content.now.initialize(context, parent = this, ChoiceEditorContent)
        result = _content.flatMap { it.result }
    }

    /**
     * Select the given [choice]
     */
    override fun select(choice: C) {
        if (!isInitialized) {
            selectInitial(choice)
            return
        }
        if (choice == selected.now) return
        val editor = createEditor(choice)
        select(choice, editor)
    }

    fun select(choice: C, editor: E) {
        if (!isInitialized) {
            selectInitial(choice, editor)
            return
        }
        doSelect(choice, editor)
        notifyViews { selected(choice, editor) }
    }

    private fun doSelect(choice: C, editor: E) {
        editor.initialize(context, parent = this, ChoiceEditorContent)
        _selected.set(choice)
        _content.set(editor)
    }

    override fun toString(choice: C): ReactiveString = reactiveValue(choice.toString())

    protected abstract fun createEditor(choice: C): E

    override fun viewAdded(view: ChoiceEditorView<C, E>) {
        view.selected(selected.now, content.now)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor is ChoiceEditorContent) {
            return content.now
        }
        return super.getSubEditor(accessor)
    }

    protected open fun fixedEditorClass(option: C): KClass<*>? = null

    override fun serialize(): JsonElement = buildJsonObject {
        put("option", toJson(selected.now))
        val typeTag = fixedEditorClass(selected.now) == null
        put("editor", content.now.serialize(typeTag))
    }

    override fun deserialize(element: JsonElement) {
        val option = fromJson(element.jsonObject.getValue("option"))
        val editorElement = element.jsonObject.getValue("editor")
        val klass = fixedEditorClass(option)
        val editor = Editor.deserialize(editorElement, klass)
        _selected = reactiveVariable(option)
        _content = reactiveVariable(editor as E)
    }
}