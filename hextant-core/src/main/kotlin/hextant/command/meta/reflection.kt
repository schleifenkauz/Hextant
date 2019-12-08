/**
 * @author Nikolaus Knop
 */

package hextant.command.meta

import hextant.command.*
import hextant.command.Command.Category
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod

internal fun <R : Any> KClass<R>.collectProvidedCommands(): Set<Command<R, *>> {
    val dest = mutableSetOf<Command<R, *>>()
    for (fct in memberFunctions) {
        val ann = fct.findAnnotation<ProvideCommand>() ?: continue
        val cmd = fct.extractCommand(this, ann)
        dest.add(cmd)
    }
    return dest
}

private fun <R : Any> KFunction<*>.extractCommand(receiver: KClass<R>, ann: ProvideCommand): Command<R, *> {
    javaMethod?.isAccessible = true
    val name = ann.name.takeIf { it != DEFAULT } ?: this.name
    val cat = ann.category.takeIf { it != DEFAULT }?.let { Category.withName(it) }
    val shortName = ann.shortName.takeIf { it != DEFAULT } ?: this.name
    val params = valueParameters.map { it.extractParameter() }
    val desc = ann.description.takeIf { it != DEFAULT } ?: "No description provided"
    val alwaysApplicable: (Any) -> Boolean = { true }
    return CommandImpl(name, cat, shortName, params, desc, this::executeCommand, alwaysApplicable, receiver)
}

private fun KFunction<*>.executeCommand(receiver: Any, args: List<Any?>): Any? {
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
        callBy(map)
    } catch (ex: IllegalCallableAccessException) {
        throw ex.cause ?: RuntimeException("Exception while executing $this")
    } catch (ex: InvocationTargetException) {
        throw ex.cause ?: RuntimeException("Exception while executing $this")
    }
}

private fun KParameter.extractParameter(): Command.Parameter {
    val ann = findAnnotation<CommandParameter>()
    val name = ann?.name?.takeIf { it != DEFAULT } ?: this.name ?: throw Exception("Parameter $this has no name")
    val cls = type.classifier as? KClass<*> ?: throw Exception("Type of parameter $this is illegal")
    val desc = ann?.description ?: "No description provided"
    return Command.Parameter(name, cls, this.isOptional || type.isMarkedNullable, desc)
}
