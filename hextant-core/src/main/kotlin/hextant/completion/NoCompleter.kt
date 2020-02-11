package hextant.completion

object NoCompleter : Completer<Any?, Nothing> {
    override fun completions(context: Any?, input: String): Collection<Completion<Nothing>> = emptySet()
}