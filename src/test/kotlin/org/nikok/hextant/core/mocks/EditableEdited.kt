/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.mocks

import org.nikok.hextant.core.base.AbstractEditable
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue

internal class EditableEdited(val arg: Edited = Edited, val otherArg: Int = 0) : AbstractEditable<Nothing>() {
    override val edited: ReactiveValue<Nothing?>
        get() = throw AssertionError()
    override val isOk: ReactiveBoolean
        get() = throw AssertionError()
}