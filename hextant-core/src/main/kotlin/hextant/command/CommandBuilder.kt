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
import kotlin.reflect.full.isSubclassOf

/**
 * Builder for [Command]
 */
@Builder
class CommandBuilder<R : Any, T : Any> @PublishedApi internal constructor(private val cls: KClass<R>) {
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
     * The [Command.shortcut] of the build command
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
     * * Defaults to [Command.Type.SingleReceiver]
     */
    var type: Command.Type = Command.Type.SingleReceiver

    private lateinit var execute: (R, CommandArguments) -> T

    private var applicable: (R) -> Boolean = { true }

    @PublishedApi internal val parameters: MutableList<ParameterBuilder<*>> = LinkedList()

    /**
     * Indicates whether the built command should be enabled by default.
     */
    var initiallyEnabled = true

    /**
     * Sets the executed function of the built command to [block]
     */
    fun executing(block: (R, CommandArguments) -> T) {
        execute = block
    }

    /**
     * Sets the executed function of the built command to [block]
     */
    @JvmName("executingWithArgumentsReceiver")
    inline fun executing(crossinline block: CommandArguments.(R) -> T) {
        executing { rec, args -> args.block(rec) }
    }

    /**
     * Adds a [Command.Parameter] build with [build] to the built command
     */
    inline fun <reified P : Any> addParameter(build: ParameterBuilder<P>.() -> Unit): Command.Parameter<P> {
        val param = parameter(build)
        parameters.add(ParameterBuilder(P::class).apply(build))
        return param
    }

    fun parameter(index: Int): ParameterBuilder<*> = parameters[index]

    fun parameter(name: String): ParameterBuilder<*> = parameters.first { it.name == name }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified P: Any> ParameterBuilder<*>.ofType(): ParameterBuilder<P> {
        check(P::class.isSubclassOf(this.type)) { "Attempt to cast type of parameter $name to ${P::class}" }
        return this as ParameterBuilder<P>
    }

    /**
     * Adds parameters built with [block] to the build command
     */
    inline fun addParameters(block: ParametersBuilder.() -> Unit) {
        parameters.addAll(ParametersBuilder().apply(block).build())
    }

    /**
     * Causes the built command to be only applicable on receivers satisfying the specified [predicate]
     */
    fun applicableIf(predicate: (R) -> Boolean) {
        applicable = predicate
    }

    @PublishedApi internal fun build(): Command<R, T> = CommandImpl(
        name, category, defaultShortcut, shortName,
        parameters.map { it.build() }, description, type, execute,
        applicable, cls, initiallyEnabled
    )
}