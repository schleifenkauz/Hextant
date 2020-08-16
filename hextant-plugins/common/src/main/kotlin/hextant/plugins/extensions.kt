/**
 * @author Nikolaus Knop
 */

@file:Suppress("BlockingMethodInNonBlockingContext")

package hextant.plugins

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.jar.JarFile

@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified T : Any> JarFile.getInfo(file: String): T? {
    val entry = getEntry(file) ?: return null
    val reader = getInputStream(entry).bufferedReader()
    return Json.parse(serializer(), reader.readText())
}

inline fun <reified T : Any> Marketplace.getInfo(id: String, file: String): T? {
    val f = getJarFile(id) ?: return null
    val jar = JarFile(f)
    return jar.getInfo(file)
}

fun Marketplace.getProjectTypes(pluginId: String): List<ProjectType>? = getInfo(pluginId, "projectTypes.json")

fun Marketplace.getImplementations(id: String): List<Implementation>? = getInfo(id, "implementations.json")

fun Marketplace.getFeatures(pluginId: String): List<Feature>? = getInfo(pluginId, "features.json")

fun Marketplace.getAspects(pluginId: String): List<Aspect>? = getInfo(pluginId, "aspects.json")

fun Marketplace.getPluginById(id: String): Plugin? = getInfo(id, "plugin.json")