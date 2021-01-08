/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.executeSafely
import hextant.core.Editor
import reaktive.dependencies
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding
import reaktive.value.now
import validated.Validated
import validated.ifInvalid

/**
 * Receiver used by [composeResult] to give easy access to the results of component editors.
 */
class ResultComposer @PublishedApi internal constructor(private val compound: Editor<*>) {
    private fun terminate(): Nothing = throw InvalidSubResultException()

    /**
     * Return the current result of the given [Editor] or immediately terminate result composition if it is not valid.
     *
     * @throws IllegalArgumentException if the given editor is not a child of the editor for which the result is composed.
     */
    @JvmName("getValidated")
    fun <R> Editor<Validated<R>>.get(): R = result.now.ifInvalid { terminate() }

    /**
     * Return the current result of the given [Editor] or immediately terminate result composition if it is null.
     *
     * @throws IllegalArgumentException if the given editor is not a child of the editor for which the result is composed.
     */
    @JvmName("getNullable")
    fun <R : Any> Editor<R?>.get(): R = result.now ?: terminate()

    /**
     * Return the current result of the given [Editor].
     *
     * @throws IllegalArgumentException if the given editor is not a child of the editor for which the result is composed.
     */
    @JvmName("getDefault")
    fun <R : Any> Editor<R>.get(): R = result.now

    /**
     * Alias for [get].
     */
    @get: JvmName("nowValidated")
    val <R> Editor<Validated<R>>.now
        get() = get()

    /**
     * Alias for [get].
     */
    @get: JvmName("nowNullable")
    val <R : Any> Editor<R?>.now
        get() = get()

    /**
     * Alias for [get].
     */
    @get: JvmName("nowDefault")
    val <R : Any> Editor<R>.now
        get() = get()
}

@PublishedApi internal class InvalidSubResultException : Exception()


/**
 * Composes a result from the results of the given component editors.
 * The result is updated every time one of the [components] changes its result
 * and if any of the component results is incomplete the compound result set to the specified [default].
 */
inline fun <R> Editor<*>.composeResult(
    components: Collection<Editor<*>>,
    crossinline default: () -> R,
    crossinline compose: ResultComposer.() -> R
): ReactiveValue<R> = binding(dependencies(components.map { it.result })) {
    context.executeSafely("compose result", default) {
        try {
            ResultComposer(this).compose()
        } catch (ex: InvalidSubResultException) {
            default()
        }
    }
}

/**
 * Vararg version of [composeResult].
 */
inline fun <R> Editor<*>.composeResult(
    vararg components: Editor<*>,
    crossinline default: () -> R,
    crossinline compose: ResultComposer.() -> R
) = composeResult(components.asList(), default, compose)
