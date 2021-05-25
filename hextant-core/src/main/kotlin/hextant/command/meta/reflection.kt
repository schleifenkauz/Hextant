/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.command.meta

import hextant.command.*
import hextant.command.Command.Category
import hextant.core.Editor
import hextant.fx.shortcut
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

internal fun <R : Any> KClass<R>.collectProvidedCommands(): Set<Command<R, *>> {
    val dest = mutableSetOf<Command<R, *>>()
    for (fct in declaredMemberFunctions) {
        if (fct.hasAnnotation<ProvideCommand>()) {
            val cmd = extractCommand(fct, this)
            dest.add(cmd)
        }
    }
    return dest
}

private fun <R : Any> extractCommand(function: KFunction<*>, receiver: KClass<R>): Command<R, *> {
    check(!function.returnType.isMarkedNullable) { "$function has nullable return type" }
    return command<R, Any>(receiver) {
        extract(function as KFunction<Any>)
    }
}

fun <R : Any, T : Any> CommandBuilder<R, T>.extract(function: KFunction<T>) {
    function.javaMethod?.isAccessible = true
    val ann = function.findAnnotation<ProvideCommand>()
    name = ann?.name.takeIf { it != DEFAULT } ?: function.name
    shortName = when (val n = ann?.shortName) {
        DEFAULT -> function.name
        NONE -> null
        null -> name
        else    -> n
    }
    category = ann?.category.takeIf { it != NONE }?.let { Category.withName(it) }
    defaultShortcut = ann?.defaultShortcut.takeIf { it != NONE }?.shortcut
    description = ann?.description.takeIf { it != NONE } ?: "No description provided"
    type = ann?.type ?: Command.Type.MultipleReceivers
    initiallyEnabled = ann?.initiallyEnabled ?: true
    executing { receiver, args -> function.executeCommand(receiver, args) }
    addParameters {
        for (parameter in function.valueParameters) extractParameter(parameter)
    }
}

private fun <T> KFunction<T>.executeCommand(receiver: Any, args: List<Any?>): T {
    val map = mutableMapOf<KParameter, Any?>()
    map[instanceParameter!!] = receiver
    for (i in this.valueParameters.indices) {
        val p = valueParameters[i]
        val a = args[i]
        if (a == NotProvided) {
            if (!p.isOptional) {
                if (!p.type.isMarkedNullable) {
                    throw ArgumentMismatchException("Non-optional and non-nullable parameter $p needs a value")
                } else {
                    map[p] = null
                }
            }
        } else {
            map[p] = a
        }
    }
    return try {
        callBy(map)!!
    } catch (ex: IllegalCallableAccessException) {
        throw ex.cause ?: RuntimeException("Exception while executing $this")
    } catch (ex: InvocationTargetException) {
        throw ex.cause ?: RuntimeException("Exception while executing $this")
    }
}

private fun ParametersBuilder.extractParameter(parameter: KParameter) = add(parameter.type.jvmErasure) {
    val ann = parameter.findAnnotation<CommandParameter>()
    name = ann?.name?.takeIf { it != DEFAULT } ?: parameter.name ?: throw Exception("Parameter $this has no name")
    description = ann?.description?.takeIf { it != NONE } ?: "No description provided"
    if (ann?.editWith ?: Default::class != Default::class) {
        editWith(ann?.editWith as KClass<out Editor<Nothing>>)
    }
}


