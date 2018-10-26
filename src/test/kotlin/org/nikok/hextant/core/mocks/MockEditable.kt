package org.nikok.hextant.core.mocks

import org.nikok.hextant.Editable
import org.nikok.reaktive.value.*

internal class MockEditable(
    override val edited: ReactiveValue<Unit> = reactiveValue("mock", Unit)
) : Editable<Unit> {
    override val isOk: ReactiveBoolean = reactiveValue("isOK", true)
}