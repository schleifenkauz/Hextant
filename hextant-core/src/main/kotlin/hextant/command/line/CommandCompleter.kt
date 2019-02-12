/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import reaktive.set.ReactiveSet

internal class CommandCompleter(pool: ReactiveSet<Command<*, *>>) : ConfiguredCompleter<Command<*, *>>(
    CompletionStrategy.simple,
    CommandCompletionFactory,
    pool,
    stringFormatter = { it.shortName!! }
)