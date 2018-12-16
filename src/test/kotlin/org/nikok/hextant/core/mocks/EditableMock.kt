package org.nikok.hextant.core.mocks

import org.nikok.hextant.core.base.AbstractEditable
import org.nikok.reaktive.value.*

internal open class EditableMock(
    override val edited: ReactiveValue<Unit> = reactiveValue("mock", Unit)
) : AbstractEditable<Unit>() {
    override val isOk: ReactiveBoolean = reactiveValue("isOK", true)
}