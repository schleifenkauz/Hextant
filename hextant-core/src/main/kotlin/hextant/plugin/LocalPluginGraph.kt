package hextant.plugin

import hextant.plugins.PluginInfo
import kollektion.graph.ImplicitGraph
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class LocalPluginGraph private constructor(private val infos: Map<String, PluginInfo>) :
    ImplicitGraph<PluginInfo>(infos.values) {
    override fun edgesFrom(value: PluginInfo): Iterable<PluginInfo> =
        value.dependencies.map { dep -> infos.getValue(dep.id) }

    companion object {
        fun load(classLoader: ClassLoader): LocalPluginGraph {
            val infos = classLoader.getResources("plugin.json").toList()
                .mapNotNull { url -> Json.decodeFromString<PluginInfo>(url.readText()) }
                .associateBy { info -> info.id }
            return LocalPluginGraph(infos)
        }
    }
}