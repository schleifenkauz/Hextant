/**
 *@author Nikolaus Knop
 */

package hextant.completion

import hextant.completion.CompletionResult.Match

/**
 * A base class for [Completer]s
 * @constructor
 * @param strategy the used [CompletionStrategy]
 * @param factory the used [CompletionFactory]
 * @param pool the pool from which completions are chosen
 * @param stringFormatter the function used to convert [C]'s to strings, defaults to `toString`
 */
open class ConfiguredCompleter<C : Any>(
    private val strategy: CompletionStrategy,
    private val factory: CompletionFactory<C>,
    private val pool: () -> Set<C>,
    private val stringFormatter: (C) -> String = Any::toString
) : Completer<C> {

    override fun completions(input: String): Set<Completion<C>> {
        val completions = pool().asSequence().mapNotNull {
            val text = stringFormatter(it)
            val result = strategy.match(input, text)
            if (result !is Match) null
            else factory.getCompletion(result, text, it)
        }
        return completions.toSet()
    }
}