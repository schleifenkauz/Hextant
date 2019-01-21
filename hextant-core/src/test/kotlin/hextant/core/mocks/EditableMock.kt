package hextant.core.mocks

import hextant.base.AbstractEditable
import reaktive.value.*

internal open class EditableMock(
    override val edited: ReactiveValue<Unit> = reactiveValue(Unit)
) : AbstractEditable<Unit>() {
    override val isOk: ReactiveBoolean = reactiveValue(true)
}