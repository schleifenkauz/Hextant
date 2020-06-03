/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A completer that uses a specific completion [strategy] to get completions from a completion pool.
 */
abstract class ConfiguredCompleter<in Ctx, T>(private val strategy: CompletionStrategy) : Completer<Ctx, T> {
    /**
     * Return a collection of possible completions in the given [context].
     */
    protected abstract fun completionPool(context: Ctx): Collection<T>

    /**
     * Extract the textual representation of the given [item]
     */
    protected open fun extractText(context: Ctx, item: T): String? = item.toString()

    /**
     * Can be overridden by extending classes to configure completions.
     */
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

    companion object {
        /**
         * Return a [ConfiguredCompleter] that uses the specified [strategy] and the given completion [pool] of strings.
         */
        fun withStringPool(strategy: CompletionStrategy, pool: Collection<String>) =
            object : ConfiguredCompleter<Any?, String>(strategy) {
                override fun completionPool(context: Any?): Collection<String> = pool
            }
    }
}