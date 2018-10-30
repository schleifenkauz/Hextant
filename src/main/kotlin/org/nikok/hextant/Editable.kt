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

    val isOk: ReactiveBoolean

    val parent: Editable<*>? get() = null

    val children: Collection<Editable<*>>? get() = null
}