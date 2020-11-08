/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.command.Command.Parameter
import kotlin.reflect.cast

/**
 * A type checked collection of command arguments that is passed to the command to execute.
 */
class CommandArguments internal constructor(parameters: List<Parameter<*>>, args: List<Any>) : List<Any> by args {
    private val map = mutableMapOf<Parameter<*>, Any>()

    init {
        if (args.size > parameters.size) throw ArgumentMismatchException("To many arguments for command $this")
        if (args.size < parameters.size) throw ArgumentMismatchException("To few arguments for command $this")
        for ((a, p) in args.zip(parameters)) {
            if (!p.type.isInstance(a)) throw ArgumentMismatchException("Parameter $a is not an instance of ${p.type}")
            map[p] = a
        }
    }

    /**
     * Return the argument that was passed for the given [parameter].
     */
    operator fun <T : Any> get(parameter: Parameter<T>): T = parameter.type.cast(map[parameter])
}