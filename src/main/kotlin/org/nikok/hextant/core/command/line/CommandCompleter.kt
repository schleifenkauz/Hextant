/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.completion.CompletionStrategy
import org.nikok.hextant.core.completion.ConfiguredCompleter

internal object CommandCompleter : ConfiguredCompleter<Command<*, *>>(
    CompletionStrategy.simple,
    CommandCompletionFactory
) {
    override fun Command<*, *>.getText(): String = shortName!!
}