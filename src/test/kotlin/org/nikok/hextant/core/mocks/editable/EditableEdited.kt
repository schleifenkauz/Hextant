package org.nikok.hextant.core.mocks.editable

import org.nikok.hextant.Editable
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue

internal object EditableEdited: Editable<Nothing> {
    override val edited: ReactiveValue<Nothing?>
        get() = throw AssertionError()
    override val isOk: ReactiveBoolean
        get() = throw AssertionError()
}