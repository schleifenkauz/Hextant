/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import org.nikok.hextant.Editable
import org.nikok.hextant.ParentEditable
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.binding.impl.notNull

abstract class AbstractEditable<out E> : Editable<E> {
    final override var parent: Editable<*>? = null
        private set

    override val isOk: ReactiveBoolean by lazy { edited.notNull() }

    override fun moveTo(newParent: ParentEditable<*, *>) {
        if (parent == newParent) return
        newParent.accept(this)
        parent = newParent
    }
}