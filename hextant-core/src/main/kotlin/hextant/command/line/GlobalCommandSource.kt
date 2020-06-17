/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.Context
import hextant.command.Command
import hextant.command.Commands
import reaktive.value.ReactiveBoolean
import reaktive.value.reactiveValue

internal class GlobalCommandSource(private val platform: Context) : CommandSource {
    override fun executeCommand(command: Command<*, *>, arguments: List<Any?>): List<Any?> {
        check(command.isApplicableOn(platform)) { "Command $command is not applicable on global context" }
        @Suppress("UNCHECKED_CAST") //checked
        command as Command<Context, Any?>
        return listOf(command.execute(platform, arguments))
    }

    override fun availableCommands(): Collection<Command<*, *>> = platform[Commands].applicableOn(platform)

    override fun isApplicable(command: Command<*, *>): ReactiveBoolean = reactiveValue(command.isApplicableOn(platform))
}