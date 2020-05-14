/**
 * @author Nikolaus Knop
 */

package hextant.command

import hextant.Editor
import hextant.command.Command.ParameterBuilder
import hextant.undo.compoundEdit
import kotlin.reflect.KClass


/**
 * Build a [Command]
 */
inline fun <R : Any, T> command(cls: KClass<R>, block: CommandBuilder<R, T>.() -> Unit): Command<R, T> =
    CommandBuilder<R, T>(cls).apply(block).build()

/**
 * Build a [Command]
 */
inline fun <reified R : Any, T> command(block: CommandBuilder<R, T>.() -> Unit): Command<R, T> =
    command(R::class, block)


/**
 * Build a [Command.Parameter]
 */
inline fun parameter(block: ParameterBuilder.() -> Unit) = ParameterBuilder().apply(block).build()

/**
 * Build [Command.Parameter]s
 */
inline fun parameters(block: ParametersBuilder.() -> Unit) = ParametersBuilder().apply(block).build()


/**
 * @return all [Command]s applicable on the specified [receiver]
 */
fun <R : Any> Commands.applicableOn(receiver: R): Set<Command<R, Any?>> {
    val reg = of(receiver::class)
    return reg.commandsFor(receiver)
}

/**
 * Register a command built with [build]
 */
inline fun <reified R : Any, T> CommandRegistrar<R>.register(build: CommandBuilder<R, T>.() -> Unit) {
    register(command(build))
}

/**
 * Makes the resulting command execute the given action combining the done edits
 * to a compound edit with the name of this command as the action description.
 */
inline fun <E : Editor<*>, T> CommandBuilder<E, T>.executingCompoundEdit(crossinline actions: (E, List<Any?>) -> T) {
    executing { e, list ->
        e.context.compoundEdit(name) { actions(e, list) }
    }
}

/**
 * Object used to indicate that a certain parameter of a command should be given a default value
 */
object NotProvided