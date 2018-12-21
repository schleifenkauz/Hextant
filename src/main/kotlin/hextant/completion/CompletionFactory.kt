/**
 * @author Nikolaus Knop
 */

package hextant.completion

/**
 * Used to create a completion from a completed element
 */
interface CompletionFactory<T> {
    /**
     * @return the [Completion] for the specified [completed] element
     */
    fun getCompletion(completed: T): Completion<T>

    companion object {
        operator fun <T> invoke(completion: (T) -> Completion<T>) = object : CompletionFactory<T> {
            override fun getCompletion(completed: T): Completion<T> = completion(completed)
        }

        fun <T> simple(stringConverter: (T) -> String = { it.toString() }) =
            CompletionFactory<T> { SimpleCompletion(it, stringConverter(it)) }

        fun simple() = simple<String> { it }
    }
}