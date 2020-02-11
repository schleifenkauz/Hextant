/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.AbstractCompleter
import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy

internal object CommandCompleter : AbstractCompleter<CommandLine, Command<*, *>>(CompletionStrategy.simple) {
    override fun completionPool(context: CommandLine): Set<Command<*, *>> = context.availableCommands()

    override fun extractText(context: CommandLine, item: Command<*, *>): String? = item.shortName

    override fun Builder<Command<*, *>>.configure(context: CommandLine) {
        tooltipText = completion.toString()
        infoText = completion.name
    }
}