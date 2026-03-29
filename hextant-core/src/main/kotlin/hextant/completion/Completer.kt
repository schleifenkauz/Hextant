/**
 * @author Nikolaus Knop
 */

package hextant.completion

import kotlinx.coroutines.CoroutineScope

/**
 * Used to get [Completion]s
 */
interface Completer<in Ctx> {
    /**
     * @return the possible completions for [input]
     */
    suspend fun CoroutineScope.collectCompletions(context: Ctx, input: String, collector: CompletionCollector)
}