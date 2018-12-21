package hextant.core.mocks

import hextant.base.AbstractEditable
import org.nikok.reaktive.value.*

internal open class EditableMock(
    override val edited: ReactiveValue<Unit> = reactiveValue("mock", Unit)
) : AbstractEditable<Unit>() {
    override val isOk: ReactiveBoolean = reactiveValue("isOK", true)
}