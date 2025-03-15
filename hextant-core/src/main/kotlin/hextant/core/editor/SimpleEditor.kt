package hextant.core.editor

import hextant.core.Editor
import reaktive.value.ReactiveValue
import reaktive.value.now
import reaktive.value.reactiveVariable

open class SimpleEditor<R : Any>(result: R) : AbstractEditor<R, SimpleEditor.View<R>>() {
    private var _result = reactiveVariable(result)

    override val result: ReactiveValue<R> get() = _result

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