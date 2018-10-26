/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import org.nikok.hextant.core.command.Command
import java.util.*

/**
 * A command application that applies [command] to the given [args]
*/
class CommandApplication<T>(val command: Command<T, *>, val args: Array<Any>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandApplication<*>

        if (command != other.command) return false
        if (!Arrays.equals(args, other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + Arrays.hashCode(args)
        return result
    }

    override fun toString(): String = buildString {
        append(command.shortName)
        for ((p, a) in command.parameters.zip(args)) {
            append(" ")
            append(p.name)
            append(": ")
            append(a)
        }
    }
}