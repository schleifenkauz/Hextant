/**
 * @author Nikolaus Knop
 */

package hextant.completion

/**
 * Used to get [Completion]s
 */
interface Completer<in Ctx> {
    /**
     * @return the possible completions for [input]
     */
    suspend fun collectCompletions(context: Ctx, input: String, collector: CompletionCollector)
}