/**
 * @author Nikolaus Knop
 */

package hextant

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
}