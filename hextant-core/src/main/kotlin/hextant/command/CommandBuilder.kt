/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.command.Command.Category
import hextant.command.Command.ParameterBuilder
import java.util.*
import kotlin.reflect.KClass

/**
 * Builder for [Command]
 */
@Builder
class CommandBuilder<R : Any, T> @PublishedApi internal constructor(private val cls: KClass<R>) {
    /**
     * The name of the built command
    */
    lateinit var name: String
    /**
     * The description of the built command
     * * Defaults to "No description specified"
     */
    var description: String = "No description specified"

    /**
     * The [Command.shortName] of the built command
    */
    var shortName: String? = null

    /**
     * The [Command.category] of the built command
    */
    var category: Category? = null

    private lateinit var execute: (R, List<Any?>) -> T
    private var applicable: (R) -> Boolean = { true }
    @PublishedApi internal val parameters: MutableList<Command.Parameter> = LinkedList()

    /**
     * Sets the executed function of the built command to [block]
    */
    fun executing(block: (R, List<Any?>) -> T) {
        execute = block
    }

    /**
     * Adds the specified [parameter] to the built command
    */
    fun addParameter(parameter: Command.Parameter) {
        parameters.add(parameter)
    }

    /**
     * Adds a [Command.Parameter] build with [build] to the built command
    */
    inline fun addParameter(build: ParameterBuilder.() -> Unit) {
        addParameter(parameter(build))
    }

    /**
     * Adds parameters built with [block] to the build command
    */
    inline fun addParameters(block: ParametersBuilder.() -> Unit) {
        parameters.addAll(parameters(block))
    }

    /**
     * Causes the built command to be only applicable on receivers satisfying the specified [predicate]
    */
    fun applicableIf(predicate: (R) -> Boolean) {
        applicable = predicate
    }

    @PublishedApi internal fun build(): Command<R, T> {
        return CommandImpl(name, category, shortName, parameters, description, execute, applicable, cls)
    }
}