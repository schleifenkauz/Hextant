package hextant.core.mocks

import hextant.Ok
import hextant.RResult
import hextant.base.AbstractEditable
import reaktive.value.reactiveValue

internal open class EditableMock(
    override val result: RResult<Unit> = reactiveValue(Ok(Unit))
) : AbstractEditable<Unit>()