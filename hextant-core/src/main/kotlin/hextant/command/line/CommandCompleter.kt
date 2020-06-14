/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

internal class CommandCompleter(private val availableCommands: () -> Collection<Command<*, *>>) :
    ConfiguredCompleter<Any?, Command<*, *>>(CompletionStrategy.simple) {
    override fun completionPool(context: Any?): Collection<Command<*, *>> = availableCommands()

    override fun extractText(context: Any?, item: Command<*, *>): String? = item.shortName

    override fun Builder<Command<*, *>>.configure(context: Any?) {
        tooltipText = completion.toString()
        infoText = completion.name
    }
}