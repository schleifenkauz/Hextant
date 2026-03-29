/**
 *@author Nikolaus Knop
 */

package hextant.completion

import kotlinx.coroutines.CoroutineScope

/**
 * A completer that uses a specific completion [strategy] to get completions from a completion pool.
 */
abstract class ConfiguredCompleter<in Ctx, T : Any>(private val strategy: CompletionStrategy) : Completer<Ctx> {
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

    final override suspend fun CoroutineScope.collectCompletions(
        context: Ctx, input: String,
        collector: CompletionCollector
    ) {
        for (completion in completionPool(context)) {
            val text = extractText(context, completion) ?: continue
            val result = strategy.match(input, text)
            if (result !is CompletionResult.Match) continue
            collector.offerCompletion(result.similarity) {
                val builder = Completion.Builder(
                    completion, input, text,
                    result.matchedRegions, result.similarity,
                    source = this@ConfiguredCompleter
                )
                builder.configure(context)
                builder.build()
            }
        }
        collector.finished()
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