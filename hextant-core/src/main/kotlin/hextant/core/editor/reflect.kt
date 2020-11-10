/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.allSupertypes
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

@PublishedApi internal fun <T : Any> List<T?>.takeIfNoNulls(): List<T>? = map { it ?: return null }

internal fun Any.getTypeArgument(superclass: KClass<*>, index: Int): KType {
    val supertype = this::class.allSupertypes.first { it.classifier == superclass }
    return supertype.arguments[index].type!!
}