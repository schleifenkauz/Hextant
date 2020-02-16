/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

internal object CommandCompleter : ConfiguredCompleter<CommandLine, Command<*, *>>(CompletionStrategy.simple) {
    override fun completionPool(context: CommandLine): Collection<Command<*, *>> = context.availableCommands()

    override fun extractText(context: CommandLine, item: Command<*, *>): String? = item.shortName

    override fun Builder<Command<*, *>>.configure(context: CommandLine) {
        tooltipText = completion.toString()
        infoText = completion.name
    }
}