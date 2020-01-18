/**
 * @author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor
import kotlin.reflect.KClass

fun <E : Editor<*>> KClass<E>.getSimpleConstructor(): (Context) -> E {
    val cstr = this.constructors.find { cstr ->
        val params = cstr.parameters.filter { !it.isOptional }
        params.size == 1 && params[0].type.classifier == Context::class
    } ?: throw NoSuchMethodException("$qualifiedName.<init>(hextant.Context)")
    val param = cstr.parameters.find { it.type.classifier == Context::class }!!
    return { ctx: Context -> cstr.callBy(mapOf(param to ctx)) }
}