/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command
import reaktive.value.ReactiveBoolean

/**
 * A command source is used by the [CommandLine] to query the applicable commands.
 */
interface CommandSource {
    /**
     * Execute the given [command] with the provided [arguments] on the appropriate targets.
     */
    fun executeCommand(command: Command<*, *>, arguments: List<Any>): List<Any>

    /**
     * Returns all the commands applicable on the current targets.
     */
    fun availableCommands(): Collection<Command<*, *>>

    /**
     * Returns a [ReactiveBoolean] holding `true` only if the given command can currently be executed.
     */
    fun isApplicable(command: Command<*, *>): ReactiveBoolean
}