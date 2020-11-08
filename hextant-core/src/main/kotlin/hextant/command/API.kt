/**
 * @author Nikolaus Knop
 */

package hextant.command

import hextant.command.Command.ParameterBuilder
import hextant.core.Editor
import hextant.undo.compoundEdit
import kotlin.reflect.KClass


/**
 * Build a [Command]
 */
inline fun <R : Any, T : Any> command(cls: KClass<R>, block: CommandBuilder<R, T>.() -> Unit): Command<R, T> =
    CommandBuilder<R, T>(cls).apply(block).build()

/**
 * Build a [Command]
 */
inline fun <reified R : Any, T : Any> command(block: CommandBuilder<R, T>.() -> Unit): Command<R, T> =
    command(R::class, block)


/**
 * Build a [Command.Parameter]
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> parameter(block: ParameterBuilder<T>.() -> Unit) =
    ParameterBuilder(T::class).apply(block).build()

/**
 * Build [Command.Parameter]s
 */
inline fun parameters(block: ParametersBuilder.() -> Unit) = ParametersBuilder().apply(block).build()

/**
 * Syntactic sugar for `register(R::class, command)`
 */
inline fun <reified R : Any> Commands.register(command: Command<R, *>) {
    register(R::class, command)
}

/**
 * Register a command built with [build]
 */
inline fun <reified R : Any, T : Any> Commands.registerCommand(build: CommandBuilder<R, T>.() -> Unit) {
    register(command(build))
}

/**
 * Makes the resulting command execute the given action combining the done edits
 * to a compound edit with the name of this command as the action description.
 */
inline fun <E : Editor<*>, T : Any> CommandBuilder<E, T>.executingCompoundEdit(crossinline actions: (E, List<Any?>) -> T) {
    executing { e, arguments ->
        e.context.compoundEdit(name) { actions(e, arguments) }
    }
}

/**
 * Object used to indicate that a certain parameter of a command should be given a default value
 */
object NotProvided