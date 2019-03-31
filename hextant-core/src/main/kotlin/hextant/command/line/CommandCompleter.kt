/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

internal class CommandCompleter(pool: () -> Set<Command<*, *>>) : ConfiguredCompleter<Command<*, *>>(
    CompletionStrategy.simple,
    CommandCompletionFactory,
    pool,
    stringFormatter = { it.shortName!! }
)