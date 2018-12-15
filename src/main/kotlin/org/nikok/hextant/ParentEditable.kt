/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.core.base.AbstractEditable

abstract class ParentEditable<out E, out C : Editable<*>> : AbstractEditable<E>() {
    /**
     * @return `true` only if the specified [child] is of type [C] and can be child of this parent
     */
    abstract fun accepts(child: Editable<*>): Boolean

    @Suppress("UNCHECKED_CAST")
    internal open fun accept(child: Editable<*>) {
        require(accepts(child)) { "$this does not accept $child as a child" }
        mutableChildren.add(child as C)
    }

    private val mutableChildren: MutableCollection<C> = mutableSetOf()

    /**
     * @return the children of this [Editable]
     */
    open val children: Collection<C> get() = mutableChildren
}

