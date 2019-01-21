/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Editable
import reaktive.value.ReactiveBoolean
import reaktive.value.binding.impl.notNull

abstract class AbstractEditable<out E> : Editable<E> {
    override val isOk: ReactiveBoolean by lazy { edited.notNull() }
}