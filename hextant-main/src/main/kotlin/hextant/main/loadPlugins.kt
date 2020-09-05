/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.core.Editor
import hextant.main.plugins.PluginGraph
import hextant.main.plugins.PluginManager
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase
import hextant.plugin.PluginBuilder.Phase.*
import hextant.plugin.PluginInitializer
import hextant.plugins.*
import kollektion.graph.ImplicitGraph
import kollektion.graph.topologicalSort
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import reaktive.Observer
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

internal fun initializePlugins(plugins: Collection<Plugin>, context: Context, phase: Phase, project: Editor<*>?) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    for (plugin in order) {
        val aspects = context[Aspects]
        if (project == null) {
            val cl = context[HextantClassLoader]
            cl.addPlugin(plugin.id)
            val impls = runBlocking { plugin.implementations.await() }
            for (impl in impls) {
                aspects.addImplementation(impl, context[HextantClassLoader])
            }
        }
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(context, info)
        initializer?.apply(context, phase, project)
    }
}

@JvmName("initializePluginsById")
internal fun initializePlugins(pluginIds: Collection<String>, context: Context, phase: Phase, project: Editor<*>?) {
    val plugins = pluginIds.map { id -> context[PluginManager].getPlugin(id) }
    initializePlugins(plugins, context, phase, project)
}

internal fun disablePlugins(plugins: Collection<Plugin>, context: Context, project: Editor<*>) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    for (plugin in order.asReversed()) {
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(context, info) ?: continue
        initializer.apply(context, Disable, project = null)
        initializer.apply(context, Disable, project)
    }
}

private fun getInitializer(context: Context, info: PluginInfo): PluginInitializer? {
    if (info.initializer == null) return null
    val cls = context[HextantClassLoader].loadClass(info.initializer).kotlin
    val initializer = cls.objectInstance ?: cls.createInstance()
    check(initializer is PluginInitializer) { "Invalid initializer $cls" }
    return initializer
}

class LocalPluginGraph private constructor(private val infos: Map<String, PluginInfo>) :
    ImplicitGraph<PluginInfo>(infos.values) {
    override fun edgesFrom(value: PluginInfo): Iterable<PluginInfo> =
        value.dependencies.map { dep -> infos.getValue(dep.id) }

    companion object {
        fun load(context: Context): LocalPluginGraph {
            val infos = context[HextantClassLoader].getResources("plugin.json").toList()
                .map { url -> Json.decodeFromString<PluginInfo>(url.readText()) }
                .associateBy { info -> info.id }
            return LocalPluginGraph(infos)
        }
    }
}


/**
 * Initializes all the plugins that are present in the classpath.
 */
fun initializePluginsFromClasspath(context: Context) {
    val graph = LocalPluginGraph.load(context)
    val order = graph.topologicalSort() ?: error("cycle in dependencies")
    for (info in order) {
        val initializer = getInitializer(context, info)
        initializer?.apply(context, Enable, project = null)
        initializer?.apply(context, Initialize, project = null)
    }
    for (impls in context[HextantClassLoader].getResources("implementations.json")) {
        val implementations: List<Implementation> = Json.decodeFromString(impls.readText())
        for (impl in implementations) {
            context[Aspects].addImplementation(impl, context[HextantClassLoader])
        }
    }
}

internal fun PluginManager.autoLoadAndUnloadPluginsOnChange(context: Context, project: Editor<*>): Observer =
    enabledPlugins.observe { _, plugins ->
        runBlocking {
            initializePlugins(plugins, context, Initialize, project = null)
            initializePlugins(plugins, context, Initialize, project)
        }
    } and disabledPlugins.observe { _, plugins ->
        runBlocking { disablePlugins(plugins, context, project) }
    }

internal fun closeProject(context: Context) {
    val enabled = context[PluginManager].enabledPlugins()
    val project = context[Project].root
    for (plugin in enabled) {
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(context, info)
        initializer?.apply(context, Close, project)
        initializer?.apply(context, Close, project = null)
    }
}

private fun Aspects.addImplementation(implementation: Implementation, classLoader: ClassLoader) {
    @Suppress("UNCHECKED_CAST")
    val aspect = classLoader.loadClass(implementation.aspect).kotlin as KClass<Any>
    val case = classLoader.loadClass(implementation.feature).kotlin
    val impl = classLoader.loadClass(implementation.clazz).kotlin
    check(impl.isSubclassOf(aspect)) { "invalid implementation class $impl for aspect $aspect" }
    val instance = impl.objectInstance ?: impl.createInstance()
    implement(aspect, case, instance)
}