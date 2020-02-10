/**
 *@author Nikolaus Knop
 */

package hextant.completion

abstract class AbstractCompleter<in Ctx, T>(private val strategy: CompletionStrategy) : Completer<Ctx, T> {
    protected abstract fun completionPool(context: Ctx): Set<T>

    protected abstract fun extractText(context: Ctx, item: T): String?

    protected open fun Completion.Builder<T>.configure(context: Ctx) {}

    final override fun completions(context: Ctx, input: String): Collection<Completion<T>> {
        val completions = mutableListOf<Completion<T>>()
        for (completion in completionPool(context)) {
            val text = extractText(context, completion) ?: continue
            val result = strategy.match(input, text)
            if (result !is CompletionResult.Match) continue
            val builder = Completion.Builder(completion, input, text, result.matchedRegions)
            builder.configure(context)
            completions.add(builder.build())
        }
        return completions
    }
}