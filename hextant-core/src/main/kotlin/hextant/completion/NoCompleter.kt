package hextant.completion

/**
 * A dummy-[Completer] that has no completions in any context.
 */
object NoCompleter : Completer<Any?> {
    override suspend fun collectCompletions(
        context: Any?, input: String, collector: CompletionCollector
    ) {
    }
}