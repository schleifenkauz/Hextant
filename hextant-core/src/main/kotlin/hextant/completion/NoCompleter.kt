package hextant.completion

object NoCompleter : Completer<Nothing> {
    override fun completions(input: String): Set<Nothing> =
        emptySet()
}