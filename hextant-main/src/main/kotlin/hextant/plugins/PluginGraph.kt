/**
 *@author Nikolaus Knop
 */

package hextant.plugins

import kollektion.graph.ImplicitGraph
import kotlinx.coroutines.runBlocking

internal class PluginGraph(private val manager: PluginManager, plugins: Collection<Plugin>) :
    ImplicitGraph<Plugin>(plugins) {

    override fun edgesFrom(value: Plugin): Iterable<Plugin> =
        runBlocking { value.info.await().dependencies.map { dep -> manager.getPlugin(dep.id) } }

    companion object {
        fun fromIds(manager: PluginManager, ids: Collection<String>): PluginGraph {
            val plugins = ids.map { id -> manager.getPlugin(id) }
            return PluginGraph(manager, plugins)
        }
    }
}