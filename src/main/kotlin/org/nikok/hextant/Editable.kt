/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue

/**
 * An Editable object which can be edited by an [Editor]
 * It acts like the Model in the MVC pattern
*/
interface Editable<out E> {
    /**
     * A [ReactiveValue] holding the edited object
    */
    val edited: ReactiveValue<E?>

    /**
     * A [ReactiveValue] holding `true` if this editable and all children are ok
    */
    val isOk: ReactiveBoolean

    /**
     * @return the parent of this [Editable], defaults to null
     * * When [parent] returns `null` this indicates that this [Editable] is the root
    */
    val parent: Editable<*>? get() = null

    /**
     * @return the children of this [Editable] defaults to null
     * * When [children] return `null` this indicates that this [Editable] is a leaf
     * * When a empty collection is returned this indicates that this [Editable] could have children but at the moment doesn't have any
    */
    val children: Collection<Editable<*>>? get() = null
}