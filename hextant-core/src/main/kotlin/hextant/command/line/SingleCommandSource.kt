/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import hextant.command.Commands
import hextant.context.Context
import reaktive.value.ReactiveBoolean
import reaktive.value.reactiveValue

/**
 * A [CommandSource] that only supports commands available on a single given [receiver].
 */
class SingleCommandSource(private val context: Context, private val receiver: Any) : CommandSource {
    override fun executeCommand(command: Command<*, *>, arguments: List<Any?>): List<Any> {
        @Suppress("UNCHECKED_CAST")
        command as Command<Any, *>
        val res = command.execute(receiver, arguments)
        return listOf(res)
    }

    override fun availableCommands(): Collection<Command<*, *>> = context[Commands].applicableOn(receiver)

    override fun isApplicable(command: Command<*, *>): ReactiveBoolean = reactiveValue(command.isApplicableOn(receiver))
}