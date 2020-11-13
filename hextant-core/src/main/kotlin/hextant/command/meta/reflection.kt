/**
 * @author Nikolaus Knop
 */

package hextant.command.meta

import hextant.command.*
import hextant.command.Command.Category
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.Editor
import hextant.fx.shortcut
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

internal fun <R : Any> KClass<R>.collectProvidedCommands(): Set<Command<R, *>> {
    val dest = mutableSetOf<Command<R, *>>()
    for (fct in declaredMemberFunctions) {
        val ann = fct.findAnnotation<ProvideCommand>() ?: continue
        val cmd = fct.extractCommand(this, ann)
        dest.add(cmd)
    }
    return dest
}

private fun <R : Any> KFunction<*>.extractCommand(receiver: KClass<R>, ann: ProvideCommand): Command<R, *> {
    check(!returnType.isMarkedNullable) { "$this has nullable return type" }
    javaMethod?.isAccessible = true
    val name = ann.name.takeIf { it != DEFAULT } ?: this.name
    val cat = ann.category.takeIf { it != NONE }?.let { Category.withName(it) }
    val shortcut = ann.defaultShortcut.takeIf { it != NONE }?.shortcut
    val shortName = when (ann.shortName) {
        DEFAULT -> name
        NONE -> null
        else    -> ann.shortName
    }
    val params = valueParameters.map { it.extractParameter() }
    val desc = ann.description.takeIf { it != NONE } ?: "No description provided"
    val type = ann.type
    val execute = this::executeCommand
    val applicable: (Any) -> Boolean = { true }
    val enabled = ann.initiallyEnabled
    return CommandImpl(name, cat, shortcut, shortName, params, desc, type, execute, applicable, receiver, enabled)
}

private fun KFunction<*>.executeCommand(receiver: Any, args: List<Any?>): Any {
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

@Suppress("UNCHECKED_CAST")
private fun KParameter.extractParameter(): Command.Parameter<*> {
    val ann = findAnnotation<CommandParameter>()
    val name = ann?.name?.takeIf { it != DEFAULT } ?: this.name ?: throw Exception("Parameter $this has no name")
    val desc = ann?.description?.takeIf { it != NONE } ?: "No description provided"
    val editWith = ann?.editWith?.takeIf { it != Default::class } as? KClass<out Editor<Any>>
    val constructor = editWith?.getSimpleEditorConstructor()
    val factory = constructor?.let { c -> EditorFactory(c) }
    return Command.Parameter(name, type.jvmErasure, desc, factory)
}

internal fun <E : Any> KClass<E>.getSimpleEditorConstructor(): (Context) -> E {
    val cstr = this.constructors.find { cstr ->
        val params = cstr.parameters.filter { !it.isOptional }
        params.size == 1 && params[0].type.classifier == Context::class
    } ?: throw NoSuchMethodException("$qualifiedName.<init>(hextant.context.Context)")
    cstr.isAccessible = true
    val param = cstr.parameters.find { it.type.classifier == Context::class }!!
    return { ctx: Context -> cstr.callBy(mapOf(param to ctx)) }
}

