/**
 * @author Nikolaus Knop
 */

package hextant.serial

import bundles.bundlesSerializersModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException

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
