/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.AbstractEditable

abstract class ParentEditable<out E, out C : Editable<*>> : AbstractEditable<E>() {
    /**
     * @return `true` only if the specified [child] is of type [C] and can be child of this parent
     */
    abstract fun accepts(child: Editable<*>): Boolean

    @Suppress("UNCHECKED_CAST")
    internal open fun accept(child: Editable<*>) {
        if (!accepts(child)) {
            throw IllegalArgumentException("$this does not accept $child as a child")
        }
        mutableChildren.add(child as C)
    }

    private val mutableChildren: MutableCollection<C> = mutableSetOf()

    /**
     * @return the direct children of this [Editable]
     */
    open val children: Collection<C> get() = mutableChildren

    /**
     * Return all recursive children of this editable
     */
    val allChildren: Sequence<Editable<*>>
        get() = children.asSequence().flatMap { c ->
            if (c is ParentEditable<*, *>) c.allChildren
            else emptySequence()
        }
}

