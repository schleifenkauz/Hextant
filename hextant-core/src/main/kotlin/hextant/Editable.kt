/**
 * @author Nikolaus Knop
 */

package hextant

import reaktive.value.ReactiveValue
import reaktive.value.now

/**
 * An Editable object which can be edited by an [Editor]
 * It acts like the Model in the MVC pattern
 */
interface Editable<out E> {
    /**
     * A [ReactiveValue] holding the current compilation result
     */
    val result: ReactiveValue<CompileResult<E>>

    val isOk get() = result.now.isOk
}