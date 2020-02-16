/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command

/**
 * A command application that applies [command] to the given [args]
 * @property command the applied command
 * @property args the arguments
 */
data class CommandApplication(val command: Command<*, *>, val args: List<Any>) {
    override fun toString(): String = buildString {
        append(command.shortName)
        for ((p, a) in command.parameters.zip(args)) {
            append(" ")
            append(p.name)
            append(": ")
            append(a)
        }
    }

    /**
     * Execute the [command] on the given [receiver]
     */
    fun execute(receiver: Any): Any {
        @Suppress("UNCHECKED_CAST")
        command as Command<Any, Any>
        return command.execute(receiver, args)
    }
}