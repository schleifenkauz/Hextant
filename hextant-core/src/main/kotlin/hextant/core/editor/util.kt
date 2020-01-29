/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.isAccessible

fun <E : Editor<*>> KClass<E>.getSimpleEditorConstructor(): (Context) -> E {
    val cstr = this.constructors.find { cstr ->
        val params = cstr.parameters.filter { !it.isOptional }
        params.size == 1 && params[0].type.classifier == Context::class
    } ?: throw NoSuchMethodException("$qualifiedName.<init>(hextant.Context)")
    cstr.isAccessible = true
    val param = cstr.parameters.find { it.type.classifier == Context::class }!!
    return { ctx: Context -> cstr.callBy(mapOf(param to ctx)) }
}

fun Any.getTypeArgument(superclass: KClass<*>, index: Int): KClass<*> {
    val supertype = this::class.allSupertypes.first { it.classifier == superclass }
    val arg = supertype.arguments[index].type!!
    return arg.classifier as KClass<*>
}