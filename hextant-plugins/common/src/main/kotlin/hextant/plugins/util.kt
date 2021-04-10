/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.jar.JarFile
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> JarFile.getInfo(name: String): T? = getInfo(name, typeOf<T>())

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun <T : Any> Marketplace.getInfo(property: PluginProperty<T>, pluginId: String): T? =
    withContext(Dispatchers.IO) {
        val file = getJarFile(pluginId) ?: return@withContext null
        JarFile(file).use { jar -> jar.getInfo(property) }
    }

private fun <T : Any> JarFile.getInfo(property: PluginProperty<T>): T? = getInfo(property.file(), property.type)

@Suppress("UNCHECKED_CAST")
@PublishedApi internal fun <T : Any> JarFile.getInfo(name: String, type: KType): T? {
    val entry = getEntry(name) ?: return null
    val text = getInputStream(entry).bufferedReader().readText()
    return Json.decodeFromString(serializer(type), text) as T
}

fun getPluginInitializer(classLoader: ClassLoader, info: PluginInfo): Any? {
    if (info.initializer == null) return null
    val cls = try {
        classLoader.loadClass(info.initializer).kotlin
    } catch (ex: ClassNotFoundException) {
        System.err.println("Initializer class of plugin ${info.id} not found")
        ex.printStackTrace()
        return null
    }
    return cls.objectInstance ?: cls.createInstance()
}
