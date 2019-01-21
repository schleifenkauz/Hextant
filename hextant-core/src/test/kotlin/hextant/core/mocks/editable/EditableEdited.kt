package hextant.core.mocks.editable

import hextant.base.AbstractEditable
import reaktive.value.ReactiveBoolean
import reaktive.value.ReactiveValue

internal class EditableEdited(val arg: Any = Any(), otherArg: Int = 0) : AbstractEditable<Nothing>() {
    override val edited: ReactiveValue<Nothing?>
        get() = throw AssertionError()
    override val isOk: ReactiveBoolean
        get() = throw AssertionError()
}