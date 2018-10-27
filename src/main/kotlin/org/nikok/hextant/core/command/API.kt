/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import org.nikok.hextant.core.command.Command.ParameterBuilder
import kotlin.reflect.KFunction


/**
 * Build a [Command]
 */
inline fun <reified R : Any, T> command(block: CommandBuilder<R, T>.() -> Unit): Command<R, T> =
        CommandBuilder<R, T>(R::class).apply(block).build()

/**
 * Build a [Command.Parameter]
 */
inline fun parameter(block: ParameterBuilder.() -> Unit) = ParameterBuilder().apply(block).build()

/**
 * Build [Command.Parameter]s
 */
inline fun parameters(block: ParametersBuilder.() -> Unit) = ParametersBuilder().apply(block).build()


@PublishedApi internal fun <R : Any, T> CommandBuilder<R, T>.extractExecution(
    kFunction: KFunction<T>
) {
    executing { r, args ->
        try {
            kFunction.call(r, *args)
        } catch (ex: IllegalArgumentException) {
            throw ArgumentMismatchException("Illegal arguments", ex)
        }
    }
}


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