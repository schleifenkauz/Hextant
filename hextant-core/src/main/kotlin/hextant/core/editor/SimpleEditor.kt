package hextant.core.editor

import hextant.context.Context
import hextant.serial.Snapshot
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

class SimpleEditor<R : Any>(context: Context, result: R) : AbstractEditor<R, Any>(context) {
    override val result: ReactiveValue<R> = reactiveValue(result)


}