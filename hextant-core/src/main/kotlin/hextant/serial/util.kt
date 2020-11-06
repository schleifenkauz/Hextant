/**
 * @author Nikolaus Knop
 */

package hextant.serial

import bundles.Property
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import kotlin.reflect.KClass

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

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
internal fun getSerializer(clazz: KClass<*>): KSerializer<Any> =
    (clazz.serializerOrNull() ?: ContextualSerializer(clazz)) as KSerializer<Any>

@Serializable(with = BundleEntrySerializer::class)
internal data class BundleEntry(val property: Property<*, *, *>, val value: Any?)