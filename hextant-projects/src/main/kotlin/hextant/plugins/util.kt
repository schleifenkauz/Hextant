/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import hextant.context.Context
import hextant.context.Internal
import hextant.core.Editor
import hextant.plugins.PluginBuilder.Phase
import hextant.serial.SerialProperties.projectRoot
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import java.lang.reflect.InaccessibleObjectException
import java.net.URL
import kotlin.reflect.full.companionObjectInstance

fun Context.setProjectRoot(path: File) {
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