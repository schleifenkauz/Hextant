package hextant.core.editor

import hextant.core.Editor
import kotlinx.serialization.Serializable
import reaktive.value.ReactiveValue
import reaktive.value.ReactiveVariable
import reaktive.value.now
import reaktive.value.reactiveVariable

@Serializable
open class SimpleEditor<R : Any> : AbstractEditor<R, SimpleEditor.View<R>>() {
    private lateinit var _result: ReactiveVariable<R>

    override val result: ReactiveValue<R> get() = _result

    fun setInitialResult(value: R) {
        _result = reactiveVariable(value)
    }

    fun setResult(result: R) {
        _result.set(result)
        notifyViews { displayResult(result) }
    }

    override fun paste(editor: Editor<*>): Boolean {
        TODO("Not yet implemented")
    }

    override fun viewAdded(view: View<R>) {
        view.displayResult(result.now)
    }

    interface View<R : Any> {
        fun displayResult(result: R)
    }
}