/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import reaktive.value.now
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

internal fun <E : Any> KClass<E>.getSimpleEditorConstructor(): (Context) -> E {
    val cstr = this.constructors.find { cstr ->
        val params = cstr.parameters.filter { !it.isOptional }
        params.size == 1 && params[0].type.classifier == Context::class
    } ?: throw NoSuchMethodException("$qualifiedName.<init>(hextant.context.Context)")
    cstr.isAccessible = true
    val param = cstr.parameters.find { it.type.classifier == Context::class }!!
    return { ctx: Context -> cstr.callBy(mapOf(param to ctx)) }
}

internal fun Any.getTypeArgument(superclass: KClass<*>, index: Int): KClass<*> {
    val supertype = this::class.allSupertypes.first { it.classifier == superclass }
    val arg = supertype.arguments[index].type!!
    return arg.classifier as KClass<*>
}

/**
 * Return a [ReactiveValidated] that always holds the value of composing a value of type [R]
 * (via the primary constructor) from the results of the given [components].
 */
inline fun <reified R : Any> composeResult(vararg components: Editor<*>): ReactiveValidated<R> {
    val cls = R::class
    val results = components.map { it.result }.toTypedArray()
    val cstr = cls.primaryConstructor ?: error("$cls has no primary constructor")
    return composeReactive(*results) {
        val now = results.map { it.now.get() }.toTypedArray()
        cstr.call(*now)
    }
}