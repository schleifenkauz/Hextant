/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Editable
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.binding.impl.notNull

abstract class AbstractEditable<out E> : Editable<E> {
    override val isOk: ReactiveBoolean by lazy { edited.notNull() }
}