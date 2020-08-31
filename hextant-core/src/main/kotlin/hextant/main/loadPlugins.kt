/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.core.Editor
import hextant.main.HextantPlatform.marketplace
import hextant.main.plugins.PluginManager
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase
import hextant.plugin.PluginBuilder.Phase.Disable
import hextant.plugin.PluginBuilder.Phase.Initialize
import hextant.plugin.PluginInitializer
import hextant.plugins.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import reaktive.Observer
import kotlin.reflect.full.createInstance

internal fun loadPlugins(plugins: List<String>, context: Context, phase: Phase, project: Editor<*>?) {
    runBlocking {
        for (id in plugins) {
            loadPlugin(id, context, phase, project)
        }
    }
}

internal suspend fun loadPlugin(id: String, context: Context, phase: Phase, project: Editor<*>?) {
    val marketplace = context[marketplace]
    val aspects = context[Aspects]
    if (project == null) {
        val cl = context[HextantClassLoader]
        cl.addPlugin(id)
        val impls = marketplace.get(PluginProperty.implementations, id).orEmpty()
        for (impl in impls) {
            aspects.addImplementation(impl)
        }
    }
    val initializer = getInitializer(context, id)
    initializer?.apply(context, phase, project)
}

private suspend fun getInitializer(context: Context, id: String): PluginInitializer? {
    val plugin = context[marketplace].get(PluginProperty.info, id) ?: error("Unknown plugin '$id'")
    return createInitializer(context, plugin)
}

private fun createInitializer(
    context: Context,
    plugin: PluginInfo
): PluginInitializer? {
    if (plugin.initializer == null) return null
    val cls = context[HextantClassLoader].loadClass(plugin.initializer).kotlin
    val initializer = cls.objectInstance ?: cls.createInstance()
    check(initializer is PluginInitializer) { "Invalid initializer $cls" }
    return initializer
}

internal suspend fun disablePlugin(id: String, context: Context, project: Editor<*>) {
    val initializer = getInitializer(context, id)
    if (initializer != null) {
        initializer.apply(context, Disable, project = null)
        initializer.apply(context, Disable, project)
    }
}

/**
 * Initializes the core plugin.
 */
fun initializePluginsFromClasspath(context: Context) {
    for (plugin in context[HextantClassLoader].getResources("plugin.json")) {
        val info: PluginInfo = Json.decodeFromString(plugin.readText())
        val initializer = createInitializer(context, info)
        initializer?.apply(context, Initialize, project = null)
    }
    for (impls in context[HextantClassLoader].getResources("implementations.json")) {
        val implementations: List<Implementation> = Json.decodeFromString(impls.readText())
        for (impl in implementations) {
            context[Aspects].addImplementation(impl)
        }
    }
}

internal fun PluginManager.autoLoadAndUnloadPluginsOnChange(context: Context, project: Editor<*>): Observer =
    enabledPlugin.observe { _, plugin ->
        runBlocking {
            loadPlugin(plugin.id, context, Initialize, project = null)
            loadPlugin(plugin.id, context, Initialize, project)
        }
    } and disabledPlugin.observe { _, plugin ->
        runBlocking { disablePlugin(plugin.id, context, project) }
    }