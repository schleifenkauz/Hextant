/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Editable
import hextant.ParentEditable
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.binding.impl.notNull

abstract class AbstractEditable<out E> : Editable<E> {
    final override var parent: ParentEditable<*, *>? = null
        private set

    override val isOk: ReactiveBoolean by lazy { edited.notNull() }

    override fun moveTo(newParent: ParentEditable<*, *>?) {
        if (newParent == null) {
            parent = null
            return
        }
        if (parent == newParent) return
        newParent.accept(this)
        parent = newParent
    }
}