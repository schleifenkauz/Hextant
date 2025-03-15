package hextant.core.editor

import hextant.context.Context
import hextant.core.view.SimpleChoiceEditorView
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.Observer
import reaktive.value.ReactiveValue
import reaktive.value.ReactiveVariable
import reaktive.value.now
import reaktive.value.reactiveVariable

@Serializable
abstract class SimpleChoiceEditor<C : Any> private constructor() :
    AbstractEditor<C, SimpleChoiceEditorView<C>>(), ChoiceSource<C> {
    @Transient
    private lateinit var observer: Observer

    @Transient
    private val binders = mutableListOf<Observer>()

    private lateinit var _selected: ReactiveVariable<C>

    override val result: ReactiveValue<C> = _selected

    fun setInitial(selected: C) {
        _selected = reactiveVariable(selected)
    }

    override fun initialize(context: Context) {
        super.initialize(context)
        observer = _selected.observe { _, _, choice ->
            notifyViews { selected(choice) }
        }
    }

    /**
     * Select the given [choice]
     */
    override fun select(choice: C) {
        if (choice == result.now) return
        _selected.set(choice)
    }

    fun syncWith(variable: ReactiveVariable<C>) {
        val obs = _selected.bindBidirectional(variable)
        binders.add(obs)
    }

    override fun toString(choice: C): String = choice.toString()

    override fun fromString(str: String): C? = null

    abstract override fun choices(): List<C>

    override fun viewAdded(view: SimpleChoiceEditorView<C>) {
        view.selected(_selected.now)
    }
}