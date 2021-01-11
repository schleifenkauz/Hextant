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

/**
 * Receiver used by [composeResult] to give easy access to the results of component editors.
 */
class ResultComposer @PublishedApi internal constructor(private val compound: Editor<*>) {
    private fun Editor<*>.checkParent() {
        require(parent == compound) { "Illegal attempt to get result of $this which is not a child of $compound" }
    }

    /**
     * Return the current result of the given [Editor] or immediately terminate result composition if it is null.
     *
     * @throws IllegalArgumentException if the given editor is not a child of the editor for which the result is composed.
     */
    fun <R : Any> Editor<R?>.get(): R {
        checkParent()
        return result.now ?: throw InvalidSubResultException()
    }

    /**
     * Alias for [get].
     */
    val <R : Any> Editor<R?>.now get() = get()

    /**
     * Return the current result list of the given [ListEditor]
     * or immediately terminate result composition if any of the results is null.
     */
    fun <R : Any> ListEditor<R?, *>.get(): List<R> {
        checkParent()
        if (result.now.any { it == null }) throw InvalidSubResultException()
        @Suppress("UNCHECKED_CAST")
        return result.now as List<R>
    }

    /**
     * Alias for [get].
     */
    val <R : Any> ListEditor<R?, *>.now get(): List<R> = get()
}

@PublishedApi internal class InvalidSubResultException : Exception()


/**
 * Composes a result from the results of the given component editors.
 * The result is updated every time one of the [components] changes its result
 * and if any of the component results is incomplete the compound result set to the specified [default].
 */
inline fun <R> Editor<*>.composeResult(
    components: Collection<Editor<*>>,
    default: R,
    crossinline compose: ResultComposer.() -> R
): ReactiveValue<R> = binding(dependencies(components.map { it.result })) {
    context.executeSafely("compose result", default) {
        try {
            ResultComposer(this).compose()
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
    default: R,
    crossinline compose: ResultComposer.() -> R
): ReactiveValue<R> = composeResult(components.asList(), default, compose)
