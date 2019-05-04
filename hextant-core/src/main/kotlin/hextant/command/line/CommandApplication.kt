/**
 *@author Nikolaus Knop
 */

package hextant.command.line

import hextant.command.Command

/**
 * A command application that applies [command] to the given [args]
*/
data class CommandApplication(val command: Command<*, *>, val args: List<Any>, private val results: List<Any?>) {
    override fun toString(): String = buildString {
        append(command.shortName)
        for ((p, a) in command.parameters.zip(args)) {
            append(" ")
            append(p.name)
            append(": ")
            append(a)
        }
        if (results.size == 1 && results.first() != Unit) {
            append(" -> ")
            append(results.first())
        }
    }
}