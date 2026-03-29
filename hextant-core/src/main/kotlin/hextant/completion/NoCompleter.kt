package hextant.completion

import kotlinx.coroutines.CoroutineScope

/**
 * A dummy-[Completer] that has no completions in any context.
 */
object NoCompleter : Completer<Any?> {
    override suspend fun CoroutineScope.collectCompletions(
        context: Any?, input: String, collector: CompletionCollector
    ) {
    }
}