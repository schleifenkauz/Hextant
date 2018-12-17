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
     * @return the parent of this [Editable]
     * * When [parent] returns `null` this indicates that this [Editable] is the root
    */
    val parent: ParentEditable<*, *>?

    /**
     * Move this [Editable] to its specified [newParent] by
     * * Setting the newParent of this [Editable] to [newParent]
     * * And adding this [Editable] to the children of [newParent]
     * If this [Editable] is already a child of [newParent] this method has no effect
     * @throws IllegalArgumentException if [newParent] doesn't accept this [Editable] as a child
     */
    fun moveTo(newParent: ParentEditable<*, *>?)
}