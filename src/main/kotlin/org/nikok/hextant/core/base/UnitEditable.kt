/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import org.nikok.hextant.Editable
import org.nikok.reaktive.value.*

open class UnitEditable : Editable<Unit> {

    override val edited: ReactiveValue<Unit?>
        get() = unitValue
    override val isOk: ReactiveBoolean
        get() = ALWAYS_OK

    companion object {
        private val unitValue = reactiveValue("unit", Unit)
        private val ALWAYS_OK: ReactiveBoolean = reactiveValue("always ok", true)
    }
}