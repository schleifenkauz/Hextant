/**
 *@author Nikolaus Knop
 */

package hextant.main.plugins

import hextant.main.plugins.PluginManager.DisableConfirmation.*
import hextant.plugins.*
import kollektion.MultiMap
import java.util.*

class PluginManager(private val marketplace: Marketplace) {
    private val enabled = mutableSetOf<Plugin>()
    private val enabledIds = mutableSetOf<String>()
    private val dependentOn = MultiMap<Plugin, Plugin>()
    private val requiredByUser = mutableSetOf<Plugin>()
    private val byId = mutableMapOf<String, Plugin>()
    private val aspects = MultiMap<String, Aspect>()
    private val features = MultiMap<String, Feature>()
    private val implementation = mutableMapOf<ImplementationRequest, String?>()
    private val usedImplementations = MultiMap<String, ImplementationRequest>()
    private val usedBundles = mutableSetOf<String>()

    fun enabledPlugins(): Set<Plugin> = enabled

    fun enabledIds(): Set<String> = enabledIds + usedBundles

    private fun getPlugin(id: String) = byId.getOrPut(id) {
        marketplace.getPluginById(id) ?: throw PluginException("Cannot find plugin with id $id")
    }

    private fun addPlugin(plugin: Plugin): Boolean {
        if (!enabled.add(plugin)) return false
        enabledIds.add(plugin.id)
        for (dep in plugin.dependencies) dependentOn[getPlugin(dep.id)].add(plugin)
        return true
    }

    private fun removePlugin(p: Plugin) {
        enabled.remove(p)
        enabledIds.remove(p.id)
        requiredByUser.remove(p)
        for (dep in p.dependencies) {
            dependentOn[getPlugin(dep.id)].remove(p)
        }
    }

    private fun getDependencies(
        plugin: Plugin,
        deps: MutableSet<Plugin>,
        stack: MutableSet<String>
    ) {
        if (!stack.add(plugin.id)) {
            val cycle = "${stack.joinToString(" -> ")} -> ${plugin.id}"
            throw PluginException("Cycle in plugin dependencies: $cycle")
        }
        for (dep in plugin.dependencies) {
            val pl = getPlugin(dep.id)
            if (pl in enabled) return
            if (!deps.add(pl)) continue
            getDependencies(pl, deps, stack)
        }
        stack.remove(plugin.id)
    }

    private fun getDependents(plugin: Plugin, dependents: MutableSet<Plugin>) {
        for (dependent in dependentOn[plugin]) {
            if (dependents.add(dependent)) getDependents(dependent, dependents)
        }
    }

    private fun disableDependencies(
        plugin: Plugin,
        confirm: (Plugin) -> DisableConfirmation
    ): Set<Plugin> {
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
                    if (dependentOn[pl].isEmpty()) q.offer(pl)
                }
            }
        }
        return removed
    }

    fun enable(
        plugin: Plugin,
        confirm: (Set<Plugin>) -> Boolean
    ): Collection<Plugin>? {
        val deps = mutableSetOf<Plugin>()
        getDependencies(plugin, deps, mutableSetOf())
        val enable = deps + plugin
        val newAspects = requestedAspects(enable)
        val newFeatures = requestedFeatures(enable)
        val requiredImpls = getRequiredImplementations(newAspects, newFeatures)
        if (deps.isNotEmpty() && !confirm(deps)) return null
        aspects.putAll(newAspects)
        features.putAll(newFeatures)
        for ((aspect, feature) in requiredImpls) {
            val bundle = requireImplementation(aspect, feature)!!.bundle
            val req = ImplementationRequest(aspect.name, feature.name)
            if (bundle != null) {
                usedImplementations[bundle].add(req)
                usedBundles.add(bundle)
                for ((_, asp, feat) in marketplace.getImplementations(bundle)!!) {
                    implementation[ImplementationRequest(asp, feat)] = bundle
                }
            } else implementation[req] = null
        }
        requiredByUser.add(plugin)
        for (pl in enable) addPlugin(pl)
        return enable
    }

    private fun requestedFeatures(enable: Set<Plugin>): MultiMap<String, Feature> {
        val newCases = MultiMap<String, Feature>()
        for (case in enable.flatMapTo(mutableSetOf()) { p -> marketplace.getFeatures(p.id).orEmpty() }) {
            for (t in case.supertypes) newCases.getOrPut(t) { mutableSetOf() }.add(case)
        }
        return newCases
    }

    private fun requestedAspects(enable: Set<Plugin>): MultiMap<String, Aspect> {
        val newAspects = MultiMap<String, Aspect>()
        for (aspect in enable.flatMapTo(mutableSetOf()) { p -> marketplace.getAspects(p.id).orEmpty() }) {
            newAspects[aspect.target].add(aspect)
        }
        return newAspects
    }

    private fun getRequiredImplementations(
        newAspects: MultiMap<String, Aspect>,
        newFeatures: MultiMap<String, Feature>
    ): Set<Pair<Aspect, Feature>> {
        val requiredImpls = mutableSetOf<Pair<Aspect, Feature>>()
        for (aspect in newAspects.values.flatten()) {
            for (feature in features[aspect.target] + newFeatures[aspect.target]) {
                if (isImplemented(aspect, feature)) continue
                requireImplementation(aspect, feature) ?: continue
                requiredImpls.add(Pair(aspect, feature))
            }
        }
        for (feature in newFeatures.values.flatten()) {
            for (t in feature.supertypes) {
                for (aspect in aspects[t] + newAspects[t]) {
                    if (isImplemented(aspect, feature)) continue
                    requireImplementation(aspect, feature)
                    requiredImpls.add(Pair(aspect, feature))
                }
            }
        }
        return requiredImpls
    }

    private fun isImplemented(aspect: Aspect, feature: Feature) =
        ImplementationRequest(aspect.name, feature.name) in implementation

    private fun requireImplementation(
        aspect: Aspect,
        feature: Feature
    ): ImplementationCoord? {
        val impl = marketplace.getImplementation(aspect.name, feature.name)
        if (impl == null && !aspect.optional) {
            throw PluginException("No implementation of aspect ${aspect.name} for case ${feature.name} found")
        }
        return impl
    }

    fun disable(
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

        for (p in removed) {
            for (aspect in marketplace.getAspects(p.id).orEmpty()) {
                aspects[aspect.target].remove(aspect)
                for (feature in features[aspect.target]) {
                    disableImplementation(aspect, feature)
                }
            }
            for (feature in marketplace.getFeatures(p.id).orEmpty()) {
                for (t in feature.supertypes) {
                    features[t].remove(feature)
                    for (aspect in aspects[t]) {
                        disableImplementation(aspect, feature)
                    }
                }
            }
            for ((_, a, f) in marketplace.getImplementations(p.id).orEmpty())
                implementation.remove(ImplementationRequest(a, f))
        }
        return removed
    }

    private fun disableImplementation(aspect: Aspect, feature: Feature) {
        val req = ImplementationRequest(aspect.name, feature.name)
        val bundle = implementation[req]
        if (bundle != null) {
            usedImplementations[bundle].remove(req)
            if (usedImplementations[bundle].isEmpty()) usedBundles.remove(bundle)
        }
    }

    enum class DisableConfirmation {
        Yes, No, All, None
    }
}