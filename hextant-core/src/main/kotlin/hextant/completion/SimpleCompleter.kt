/**
 *@author Nikolaus Knop
 */

package hextant.completion

/**
 * A completer that produced [SimpleCompletion]s
 */
class SimpleCompleter<C : Any>(
    strategy: CompletionStrategy,
    pool: () -> Set<C>
) : ConfiguredCompleter<C>(strategy, CompletionFactory.simple(), pool)