package hextant.completion

/**
 * A dummy-[Completer] that has no completions in any context.
 */
object NoCompleter : Completer<Any?, Nothing> {
    override fun completions(context: Any?, input: String): Collection<Completion<Nothing>> = emptySet()
}