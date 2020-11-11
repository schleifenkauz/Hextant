/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.executeSafely
import hextant.core.Editor
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now
import validated.*
import validated.reaktive.ReactiveValidated

/**
 * Receiver used by [composeResult] to give easy access to the results of component editors.
 */
class ResultComposer @PublishedApi internal constructor(private val compound: Editor<*>) {
    /**
     * Return the current result of the given [Editor] or immediately terminate result composition if it is null.
     *
     * @throws IllegalArgumentException if the given editor is not a child of the editor for which the result is composed.
     */
    fun <R> Editor<R>.get(): R {
        require(parent == compound) { "Illegal attempt to get result of $this which is not a child of $compound" }
        return result.now.ifInvalid { throw InvalidSubResultException() }
    }

    /**
     * Alias for [get].
     */
    val <R : Any> Editor<R>.now get() = get()
}

@PublishedApi internal class InvalidSubResultException : Exception()


/**
 * Composes a result from the results of the given component editors.
 * The result is updated every time one of the [components] changes its result
 * and if any of the component results is incomplete the compound result set to the specified [default].
 */
inline fun <R> Editor<*>.composeResult(
    components: Collection<Editor<*>>,
    default: Validated<R> = invalidComponent(),
    crossinline compose: ResultComposer.() -> R
): ReactiveValidated<R> = binding<Validated<R>>(dependencies(components.map { it.result })) {
    context.executeSafely("compose result", invalid("error while assembling result")) {
        try {
            valid(ResultComposer(this).compose())
        } catch (ex: InvalidSubResultException) {
            default
        }
    }
}

/**
 * Vararg version of [composeResult].
 */
inline fun <R> Editor<*>.composeResult(
    vararg components: Editor<*>,
    default: Validated<R> = invalidComponent(),
    crossinline compose: ResultComposer.() -> R?
) = composeResult(components.asList(), default, compose)
