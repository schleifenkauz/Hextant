/**
 *@author Nikolaus Knop
 */

package hextant.command

import hextant.command.Command.Category
import hextant.command.Command.ParameterBuilder
import hextant.fx.Shortcut
import hextant.fx.shortcut
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

    /**
     * The [Command.defaultShortcut] of the build command
     */
    var defaultShortcut: Shortcut? = null

    /**
     * Set the [defaultShortcut]
     */
    fun defaultShortcut(shortcut: String) {
        defaultShortcut = shortcut.shortcut
    }

    /**
     * The [Command.Type] of the built command
     * * Defaults to [Command.Type.MultipleReceivers]
     */
    var type: Command.Type = Command.Type.MultipleReceivers

    private lateinit var execute: (R, List<Any?>) -> T

    private var applicable: (R) -> Boolean = { true }

    @PublishedApi internal val parameters: MutableList<Command.Parameter> = LinkedList()

    /**
     * Indicates whether the built command should be enabled by default.
     */
    var initiallyEnabled = true

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

    @PublishedApi internal fun build(): Command<R, T> = CommandImpl(
        name, category, defaultShortcut, shortName,
        parameters, description, type, execute,
        applicable, cls, initiallyEnabled
    )
}