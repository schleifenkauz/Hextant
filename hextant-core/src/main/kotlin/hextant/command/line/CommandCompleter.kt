/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

internal object CommandCompleter : ConfiguredCompleter<Command<*, *>>(
    CompletionStrategy.simple,
    CommandCompletionFactory
) {
    override fun Command<*, *>.getText(): String = shortName!!
}