package hextant.core.editor

import hextant.context.withoutUndo
import hextant.core.view.SimpleChoiceEditorView
import hextant.serial.JsonSerializer
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.json.JsonElement
import reaktive.Observer
import reaktive.value.*

abstract class SimpleChoiceEditor<C> : AbstractEditor<C, SimpleChoiceEditorView<C>>(), ChoiceSource<C>,
    JsonSerializer<C> {
    private val syncedVariables = mutableMapOf<ReactiveVariable<C>, Observer>()

    private lateinit var _selected: ReactiveVariable<C>

    final override val result: ReactiveValue<C> get() = _selected

    fun selectInitial(selected: C) {
        _selected = reactiveVariable(selected)
    }

    /**
     * Select the given [choice]
     */
    override fun select(choice: C) {
        val old = result.now
        if (choice == old) return
        _selected.set(choice)
        for (variable in syncedVariables.keys) {
            variable.now = choice
        }
        context[UndoManager].record(Edit(this, old, choice))
        notifyViews { selected(choice) }
    }

    fun syncWith(variable: ReactiveVariable<C>): SimpleChoiceEditor<C> {
        if (!isInitialized) selectInitial(variable.now)
        else context.withoutUndo {
            select(variable.now)
        }
        val obs = variable.observe { _, _, v ->
            context.withoutUndo {
                select(v)
            }
        }
        syncedVariables[variable] = obs
        return this
    }

    fun unsync(variable: ReactiveVariable<C>) {
        syncedVariables.remove(variable)!!.kill()
    }

    override fun toString(choice: C): ReactiveString = reactiveValue(choice.toString())

    abstract override fun choices(): List<C>

    override fun viewAdded(view: SimpleChoiceEditorView<C>) {
        view.selected(_selected.now)
    }

    override fun deserialize(element: JsonElement) {
        selectInitial(fromJson(element))
    }

    override fun serialize(): JsonElement = toJson(result.now)

    private class Edit<T>(
        private val selector: SimpleChoiceEditor<T>,
        private val old: T,
        private val new: T
    ) : AbstractEdit() {
        override val actionDescription: String
            get() = "Select"

        override fun doUndo() {
            selector.select(old)
        }

        override fun doRedo() {
            selector.select(new)
        }
    }
}