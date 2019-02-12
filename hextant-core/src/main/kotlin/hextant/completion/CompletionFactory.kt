/**
 * @author Nikolaus Knop
 */

package hextant.completion

import hextant.completion.CompletionResult.Match

/**
 * Used to create a completion from a completed element
 */
interface CompletionFactory<T> {
    /**
     * @return the [Completion] for the specified [completed] element
     */
    fun getCompletion(match: Match, completedText: String, completed: T): Completion<T>

    companion object {
        operator fun <T> invoke(completion: (Match, String, T) -> Completion<T>) = object : CompletionFactory<T> {
            override fun getCompletion(match: Match, completedText: String, completed: T): Completion<T> =
                completion(match, completedText, completed)
        }

        fun <T> simple() = CompletionFactory<T> { match, text, completed -> SimpleCompletion(completed, text, match) }
    }
}