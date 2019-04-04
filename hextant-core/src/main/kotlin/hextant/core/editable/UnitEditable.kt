/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import hextant.Ok
import hextant.RResult
import hextant.base.AbstractEditable
import reaktive.value.reactiveValue

open class UnitEditable : AbstractEditable<Unit>() {
    override val result: RResult<Unit>
        get() = unitValue

    companion object {
        private val unitValue = reactiveValue(Ok(Unit))
    }
}