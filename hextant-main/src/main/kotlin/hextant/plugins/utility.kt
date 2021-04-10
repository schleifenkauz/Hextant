/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import hextant.context.Context
import hextant.context.Internal
import hextant.core.Editor
import hextant.plugins.PluginBuilder.Phase
import hextant.serial.Files
import hextant.serial.SerialProperties.projectRoot
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import java.lang.reflect.InaccessibleObjectException
import java.net.URL
import kotlin.reflect.full.companionObjectInstance

internal fun PluginInitializer.tryApplyPhase(
    phase: Phase,
    id: String,
    context: Context,
    project: Editor<*>?,
    testing: Boolean = false
) {
    try {
        apply(context, phase, project, testing)
    } catch (ex: Throwable) {
        System.err.println("Error while applying $phase to plugin $id")
        ex.printStackTrace()
    }
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> Json.tryParse(name: String, readText: () -> String): T? = try {
    decodeFromString<T>(serializer(), readText())
} catch (ex: IOException) {
    ex.printStackTrace()
    null
} catch (ex: SerializationException) {
    System.err.println("$name is corrupted")
    ex.printStackTrace()
    null
}

fun ClassLoader.addURL(path: String) {
    val field = javaClass.getDeclaredField("ucp")
    field.isAccessible = true
    val ucp = field.get(this)
    val method = ucp.javaClass.getMethod("addFile", String::class.java)
    method.isAccessible = true
    method.invoke(ucp, path)
}

fun ClassLoader.addPlugin(id: String, context: Context) {
    if (id == "main" || id == "core") return
    val file = context[Files][Files.PLUGIN_CACHE].resolve("$id.jar")
    addURL(file.toString())
}
