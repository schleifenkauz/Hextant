/**
 *@author Nikolaus Knop
 */

package hextant.core.command.line

import hextant.core.command.Command
import hextant.core.completion.CompletionStrategy
import hextant.core.completion.ConfiguredCompleter

internal object CommandCompleter : ConfiguredCompleter<Command<*, *>>(
    CompletionStrategy.simple,
    CommandCompletionFactory
) {
    override fun Command<*, *>.getText(): String = shortName!!
}