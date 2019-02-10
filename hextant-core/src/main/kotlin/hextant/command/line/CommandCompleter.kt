/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.*

internal class CommandCompleter(pool: CompletionPool<Command<*, *>>) : ConfiguredCompleter<Command<*, *>>(
    CompletionStrategy.simple,
    CommandCompletionFactory,
    pool
) {
    override fun Command<*, *>.getText(): String = shortName!!
}