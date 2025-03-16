/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.Editor
import hextant.core.view.ChoiceEditorView
import hextant.serial.ChoiceEditorContent
import hextant.serial.EditorAccessor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.*
import reaktive.value.binding.flatMap

/**
 * An [Editor] which supports choosing different items of type [C]
 */
@Serializable
abstract class ChoiceEditor<C : Any, R, E : Editor<R>> :
    AbstractEditor<R, ChoiceEditorView<C, E>>(), ChoiceSource<C> {
    private lateinit var _selected: ReactiveVariable<C>
    private lateinit var _content: ReactiveVariable<E>

    val selected: ReactiveValue<C> get() = _selected
    val content: ReactiveValue<E> get() = _content

    @Transient
    final override lateinit var result: ReactiveValue<R>
        private set

    fun selectInitial(choice: C) {
        _selected = reactiveVariable(choice)
        _content = reactiveVariable(createEditor(choice))
    }

    override fun doInitialize() {
        content.now.initialize(context)
        content.now.locate(parent = this, ChoiceEditorContent)
        result = _content.flatMap { it.result }
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
        notifyViews { selected(choice, editor) }
    }

    private fun doSelect(choice: C, editor: E) {
        _selected.set(choice)
        _content.set(editor)
        editor.locate(parent = this, ChoiceEditorContent)
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
}