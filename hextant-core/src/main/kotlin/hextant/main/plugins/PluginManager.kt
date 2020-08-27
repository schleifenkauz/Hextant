/**
 *@author Nikolaus Knop
 */

package hextant.main.plugins

import hextant.main.plugins.PluginManager.DisableConfirmation.*
import hextant.plugins.*
import kollektion.MultiMap
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

internal class PluginManager(private val marketplace: Marketplace, private val required: Set<String>) {
    private val enabled = mutableSetOf<Plugin>()
    private val dependentOn = MultiMap<String, Plugin>()
    private val requiredByUser = mutableSetOf<Plugin>()
    private val aspects = MultiMap<String, Aspect>()
    private val features = MultiMap<String, Feature>()
    private val implementation = mutableMapOf<ImplementationRequest, String?>()
    private val usedImplementations = MultiMap<String, ImplementationRequest>()
    private val usedBundles = mutableSetOf<String>()
    private val plugins = mutableMapOf<String, Plugin>()

    init {
        for (id in required) enable(getPlugin(id)) { true }
    }

    fun enabledIds(): Set<String> = enabled.mapTo(mutableSetOf()) { it.id } + usedBundles

    fun enabledPlugins(): Set<Plugin> = enabled

    fun getPlugin(id: String) = plugins.getOrPut(id) { Plugin(id, marketplace) }

    private fun addPlugin(plugin: Plugin): Boolean {
        if (!enabled.add(plugin)) return false
        val info = plugin.info
        for (dep in info.dependencies) dependentOn[dep.id].add(plugin)
        return true
    }

    private fun removePlugin(plugin: Plugin) {
        enabled.remove(plugin)
        requiredByUser.remove(plugin)
        val info = plugin.info
        for (dep in info.dependencies) {
            dependentOn[dep.id].remove(plugin)
        }
    }

    private fun getDependencies(
        plugin: Plugin,
        deps: MutableSet<Plugin>,
        stack: MutableSet<Plugin>
    ) {
        if (!stack.add(plugin)) {
            val cycle = "${stack.joinToString(" -> ")} -> $plugin"
            throw PluginException("Cycle in plugin dependencies: $cycle")
        }
        for ((id) in plugin.info.dependencies) {
            val pl = getPlugin(id)
            if (pl in enabled) return
            if (!deps.add(pl)) continue
            getDependencies(pl, deps, stack)
        }
        stack.remove(plugin)
    }

    private fun getDependents(plugin: Plugin, dependents: MutableSet<Plugin>) {
        for (dependent in dependentOn[plugin.id]) {
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
                for ((id) in p.info.dependencies) {
                    val pl = getPlugin(id)
                    if (dependentOn[id].isEmpty()) q.offer(pl)
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
        val ownImplementations = plugin.implementations.mapTo(mutableSetOf()) { (_, aspect, feature) ->
            ImplementationRequest(aspect, feature)
        }
        val requiredImpls = getRequiredImplementations(newAspects, newFeatures, ownImplementations)
        if (deps.isNotEmpty() && !confirm(deps)) return null
        aspects.putAll(newAspects)
        features.putAll(newFeatures)
        for ((aspect, feature) in requiredImpls) {
            val impl = requireImplementation(aspect, feature) ?: continue
            val bundle = impl.bundle
            val req = ImplementationRequest(aspect.name, feature.name)
            if (bundle != null) {
                usedImplementations[bundle].add(req)
                usedBundles.add(bundle)
                for ((_, asp, feat) in marketplace.get(PluginProperty.implementations, bundle).orEmpty()) {
                    implementation[ImplementationRequest(asp, feat)] = bundle
                }
            } else implementation[req] = null
        }
        requiredByUser.add(plugin)
        for (pl in enable) addPlugin(pl)
        runBlocking {
            for (pl in enable) {
                launch {
                    marketplace.getJarFile(pl.id)
                }
            }
        }
        return enable
    }

    private fun requestedFeatures(enable: Set<Plugin>): MultiMap<String, Feature> {
        val newFeatures = MultiMap<String, Feature>()
        for (feature in enable.flatMap { it.features }) {
            for (t in feature.supertypes) newFeatures.getOrPut(t) { mutableSetOf() }.add(feature)
        }
        return newFeatures
    }

    private fun requestedAspects(enable: Set<Plugin>): MultiMap<String, Aspect> {
        val newAspects = MultiMap<String, Aspect>()
        for (aspect in enable.flatMap { it.aspects }) {
            newAspects[aspect.target].add(aspect)
        }
        return newAspects
    }

    private fun getRequiredImplementations(
        newAspects: MultiMap<String, Aspect>,
        newFeatures: MultiMap<String, Feature>,
        ownImplementations: Set<ImplementationRequest>
    ): Set<Pair<Aspect, Feature>> {
        val requiredImpls = mutableSetOf<Pair<Aspect, Feature>>()
        for (aspect in newAspects.values.flatten()) {
            for (feature in features[aspect.target] + newFeatures[aspect.target]) {
                addRequiredImplementation(aspect, feature, ownImplementations, requiredImpls)
            }
        }
        for (feature in newFeatures.values.flatten()) {
            for (t in feature.supertypes) {
                for (aspect in aspects[t] + newAspects[t]) {
                    addRequiredImplementation(aspect, feature, ownImplementations, requiredImpls)
                }
            }
        }
        return requiredImpls
    }

    private fun addRequiredImplementation(
        aspect: Aspect,
        feature: Feature,
        ownImplementations: Set<ImplementationRequest>,
        requiredImpls: MutableSet<Pair<Aspect, Feature>>
    ) {
        val req = ImplementationRequest(aspect.name, feature.name)
        if (req in ownImplementations) return
        if (req in implementation) return
        requireImplementation(aspect, feature)
        requiredImpls.add(Pair(aspect, feature))
    }

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
        val req = removed.find { it.id in required } ?: plugin.takeIf { it.id in required }
        if (req != null) throw PluginException("Cannot disable plugin ${req.info.name} as it is required")
        if (removed.isNotEmpty() && !confirm(removed)) return null
        for (pl in removed) removePlugin(pl)
        val disabled = disableDependencies(plugin, askDisable)
        removed.addAll(disabled)
        for (p in removed) {
            for (aspect in p.aspects) {
                aspects[aspect.target].remove(aspect)
                for (feature in features[aspect.target]) {
                    disableImplementation(aspect, feature)
                }
            }
            for (feature in p.features) {
                for (t in feature.supertypes) {
                    features[t].remove(feature)
                    for (aspect in aspects[t]) {
                        disableImplementation(aspect, feature)
                    }
                }
            }
            for ((_, a, f) in p.implementations) {
                implementation.remove(ImplementationRequest(a, f))
            }
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