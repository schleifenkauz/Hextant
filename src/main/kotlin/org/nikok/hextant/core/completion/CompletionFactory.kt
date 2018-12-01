/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.completion

/**
 * Used to create a completion from a completed element
 */
interface CompletionFactory<in T> {
    /**
     * @return the [Completion] for the specified [completed] element
     */
    fun getCompletion(completed: T): Completion

    companion object {
        operator fun <T> invoke(completion: (T) -> Completion) = object : CompletionFactory<T> {
            override fun getCompletion(completed: T): Completion = completion(completed)
        }

        fun <T> simple(stringConverter: (T) -> String = { it.toString() }) =
            CompletionFactory<T> { SimpleCompletion(stringConverter(it)) }
    }
}