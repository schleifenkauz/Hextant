package hextant.core.editor

import hextant.context.Context
import hextant.serial.Snapshot
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue
import reaktive.value.reactiveVariable

class SimpleEditor<R : Any>(context: Context, result: R) : AbstractEditor<R, Any>(context) {
    private var _result = reactiveVariable(result)

    override val result: ReactiveValue<R> get() = _result

    fun setResult(result: R) {
        _result.set(result)
    }
}