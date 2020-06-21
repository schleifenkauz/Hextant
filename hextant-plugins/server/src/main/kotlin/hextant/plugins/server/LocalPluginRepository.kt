/**
 *@author Nikolaus Knop
 */

package hextant.plugins.server

import hextant.plugins.Plugin
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parse
import java.io.File

class LocalPluginRepository(private val root: File) : PluginRepository {
    override fun getAllPlugins(): List<String> =
        root.list()!!.filter { it.endsWith(".json") }.map { it.removeSuffix(".json") }

    @OptIn(ImplicitReflectionSerializer::class)
    override fun getPluginById(id: String): Plugin? {
        val file = root.resolve("$id.json")
        if (!file.exists()) return null
        return json.parse(file.readText())
    }

    override fun getJarFile(pluginId: String): File? = root.resolve("$pluginId.jar").takeIf { it.exists() }

    companion object {
        private val json = Json(JsonConfiguration.Stable)
    }
}