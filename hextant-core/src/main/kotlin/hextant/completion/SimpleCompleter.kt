/**
 *@author Nikolaus Knop
 */

package hextant.completion

import reaktive.set.ReactiveSet

/**
 * A completer that produced [SimpleCompletion]s
 */
class SimpleCompleter<C : Any>(
    strategy: CompletionStrategy,
    pool: ReactiveSet<C>
) : ConfiguredCompleter<C>(strategy, CompletionFactory.simple(), pool)