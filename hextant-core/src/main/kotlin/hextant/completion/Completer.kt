/**
 * @author Nikolaus Knop
 */

package hextant.completion

/**
 * Used to get [Completion]s
 */
interface Completer<in Ctx, out T : Any> {
    /**
     * @return the possible completions for [input]
     */
    fun completions(context: Ctx, input: String): Collection<Completion<T>>
}