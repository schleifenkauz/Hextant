/**
 * @author Nikolaus Knop
 */

package hextant.serial

import bundles.bundlesSerializersModule
import hextant.context.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible

internal inline fun safeIO(action: () -> Unit) {
    try {
        action()
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}

internal val JsonElement.string
    get(): String {
        val prim = jsonPrimitive
        require(prim.isString) { "$prim is not a string" }
        return prim.content
    }

internal fun String.loadClass() = Thread.currentThread().contextClassLoader.loadClass(this)

internal val json = Json { serializersModule = bundlesSerializersModule }

internal fun <E, F : Any> Iterable<E>.firstNotNull(f: (E) -> F?): F? {
    for (e in this) {
        val x = f(e)
        if (x != null) return x
    }
    return null
}

private fun <T : Any> findFunction(
    name: String,
    functions: Collection<KFunction<T>>,
    parameterTypes: List<KType>
): (List<Any?>) -> T {
    val (func, params) = functions.firstNotNull { f ->
        val params = f.parameters.filter { !it.isOptional }
        if (params.size != parameterTypes.size) null
        else if (!params.zip(parameterTypes).all { (p, t) -> t.isSubtypeOf(p.type) }) null
        else Pair(f, params)
    } ?: throw NoSuchMethodException("$name(${parameterTypes.joinToString()})")
    func.isAccessible = true
    return { args -> func.callBy(params.zip(args).toMap()) }
}

internal fun <T : Any> KClass<T>.getConstructor(parameterTypes: List<KClass<*>>) =
    findFunction("$qualifiedName.<init>", constructors, parameterTypes.map { it.starProjectedType })

internal fun <T: Any> KClass<T>.getNoArgConstructor(): () -> T {
    val cstr = getConstructor(emptyList())
    return { cstr(emptyList()) }
}

internal fun <T: Any, A : Any> KClass<T>.getConstructor(a: KClass<A>): (A) -> T {
    val cstr = getConstructor(listOf(a))
    return { x -> cstr(listOf(x)) }
}
internal fun <T: Any, A : Any, B : Any> KClass<T>.getConstructor(a: KClass<A>, b: KClass<B>): (A, B) -> T {
    val cstr = getConstructor(listOf(a, b))
    return { x, y -> cstr(listOf(x, y)) }
}