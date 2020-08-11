/**
 *@author Nikolaus Knop
 */

package hextant.plugins

import hextant.plugins.PluginManager.DisableConfirmation.*
import java.util.*

class PluginManager(private val marketplace: Marketplace) {
    private val enabled = mutableSetOf<Plugin>()
    private val enabledIds = mutableSetOf<String>()
    private val dependentOn = mutableMapOf<Plugin, MutableSet<Plugin>>()
    private val requiredByUser = mutableSetOf<Plugin>()
    private val byId = mutableMapOf<String, Plugin>()

    fun enabledPlugins(): Set<Plugin> = enabled

    fun enabledIds(): Set<String> = enabledIds

    private fun dependentOn(plugin: Plugin) = dependentOn.getOrPut(plugin) { mutableSetOf() }

    private suspend fun getPlugin(id: String) = byId.getOrPut(id) {
        marketplace.getPluginById(id) ?: throw PluginException("Cannot find plugin with id $id")
    }

    private suspend fun addPlugin(plugin: Plugin): Boolean {
        if (!enabled.add(plugin)) return false
        enabledIds.add(plugin.id)
        for (dep in plugin.dependencies) dependentOn(getPlugin(dep.id)).add(plugin)
        return true
    }

    private suspend fun removePlugin(p: Plugin) {
        enabled.remove(p)
        enabledIds.remove(p.id)
        requiredByUser.remove(p)
        for (dep in p.dependencies) {
            dependentOn(getPlugin(dep.id)).remove(p)
        }
    }

    private suspend fun getDependencies(plugin: Plugin, deps: MutableSet<Plugin>, stack: MutableList<Plugin>) {
        if (plugin in enabled) return
        if (!stack.add(plugin)) {
            throw PluginException("Cycle in plugin dependencies: ${stack.joinToString(" -> ")} -> $plugin")
        }
        if (!deps.add(plugin)) return
        for (dep in plugin.dependencies) {
            val pl = getPlugin(dep.id)
            getDependencies(pl, deps, stack)
        }
        stack.removeAt(stack.size - 1)
    }

    private fun getDependents(plugin: Plugin, dependents: MutableSet<Plugin>) {
        for (dependent in dependentOn(plugin)) {
            if (dependents.add(dependent)) getDependents(dependent, dependents)
        }
    }

    private suspend fun disableDependencies(plugin: Plugin, confirm: (Plugin) -> DisableConfirmation): Set<Plugin> {
        val removed = mutableSetOf<Plugin>()
        val q: Queue<Plugin> = LinkedList()
        q.offer(plugin)
        var all = false
        while (q.isNotEmpty()) {
            val p = q.poll()
            val disable = when {
                p == plugin         -> true
                p in requiredByUser -> false
                all                 -> true
                else                -> when (confirm(p)) {
                    Yes -> true
                    No -> false
                    All -> {
                        all = true
                        true
                    }
                    None -> return removed
                }
            }
            if (disable) {
                removed.add(p)
                removePlugin(p)
                for (dep in p.dependencies) {
                    val pl = getPlugin(dep.id)
                    if (dependentOn(pl).isEmpty()) q.offer(pl)
                }
            }
        }
        return removed
    }

    suspend fun enable(plugin: Plugin, confirm: (Set<Plugin>) -> Boolean): Collection<Plugin>? {
        val deps = mutableSetOf<Plugin>()
        getDependencies(plugin, deps, mutableListOf())
        if (deps.size > 1 && !confirm(deps)) return null
        requiredByUser.add(plugin)
        for (pl in deps) addPlugin(pl)
        return deps
    }

    suspend fun disable(
        plugin: Plugin,
        confirm: (Set<Plugin>) -> Boolean,
        askDisable: (Plugin) -> DisableConfirmation
    ): Collection<Plugin>? {
        val removed = mutableSetOf<Plugin>()
        getDependents(plugin, removed)
        if (removed.isNotEmpty() && !confirm(removed)) return null
        for (pl in removed) removePlugin(pl)
        val disabled = disableDependencies(plugin, askDisable)
        removed.addAll(disabled)
        removed.add(plugin)
        return removed
    }

    enum class DisableConfirmation {
        Yes, No, All, None
    }
}