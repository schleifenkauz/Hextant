/**
 * @author Nikolaus Knop
 */

package hextant.launcher

import hextant.context.Context
import hextant.context.Internal
import hextant.core.Editor
import hextant.plugin.PluginBuilder.Phase
import hextant.plugin.PluginInitializer
import hextant.serial.SerialProperties.projectRoot
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import java.lang.reflect.InaccessibleObjectException
import java.net.URL
import kotlin.reflect.full.companionObjectInstance

internal fun Context.setProjectRoot(path: File) {
    val perm = Internal::class.companionObjectInstance as Internal
    set(perm, projectRoot, path)
}

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
internal inline fun <reified T : Any> Json.tryParse(name: String, readText: () -> String): T? = try {
    decodeFromString<T>(serializer(), readText())
} catch (ex: IOException) {
    ex.printStackTrace()
    null
} catch (ex: SerializationException) {
    System.err.println("$name is corrupted")
    ex.printStackTrace()
    null
}

@Suppress("UNCHECKED_CAST")
internal fun getURLs(classLoader: ClassLoader): Array<out URL> =
    try {
        val ucp = getURLClassPath(classLoader)
        val method = ucp.javaClass.getMethod("getURLs")
        method.invoke(ucp) as Array<out URL>
    } catch (e: ReflectiveOperationException) {
        throw AssertionError("Could not extract urls from system class loader", e)
    } catch (e: InaccessibleObjectException) {
        throw AssertionError("Could not extract urls from system class loader", e)
    } catch (e: ClassCastException) {
        throw AssertionError("Could not extract urls from system class loader", e)
    }

private fun getURLClassPath(classLoader: ClassLoader): Any {
    val field = classLoader.javaClass.getDeclaredField("ucp")
    field.isAccessible = true
    return field.get(classLoader)
}