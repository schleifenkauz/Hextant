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
import hextant.project.ProjectType
import kollektion.graph.ImplicitGraph
import kollektion.graph.topologicalSort
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import reaktive.Observer
import kotlin.reflect.KClass
import kotlin.reflect.full.*

internal fun addPlugins(plugins: Collection<Plugin>, context: Context, phase: Phase, project: Editor<*>?) {
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
        initializer?.tryApplyPhase(phase, id = info.id, context = context, project = project)
    }
}

@JvmName("initializePluginsById")
internal fun addPlugins(pluginIds: Collection<String>, context: Context, phase: Phase, project: Editor<*>?) {
    val plugins = pluginIds.map { id -> context[PluginManager].getPlugin(id) }
    addPlugins(plugins, context, phase, project)
}

internal fun disablePlugins(plugins: Collection<Plugin>, context: Context, project: Editor<*>?) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    for (plugin in order.asReversed()) {
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(context, info) ?: continue
        initializer.tryApplyPhase(Disable, id = info.id, context = context, project = null)
        if (project != null)
            initializer.tryApplyPhase(Disable, id = info.id, context = context, project = project)
    }
}

@JvmName("disablePluginsById")
internal fun disablePlugins(pluginIds: Collection<String>, context: Context, project: Editor<*>?) {
    val plugins = pluginIds.map { id -> context[PluginManager].getPlugin(id) }
    disablePlugins(plugins, context, project)
}

private fun getInitializer(context: Context, info: PluginInfo): PluginInitializer? {
    if (info.initializer == null) return null
    val cls = try {
        context[HextantClassLoader].loadClass(info.initializer).kotlin
    } catch (ex: ClassNotFoundException) {
        System.err.println("Initializer class of plugin ${info.id} not found")
        ex.printStackTrace()
        return null
    }
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
                .mapNotNull { url -> Json.tryParse<PluginInfo>("plugin.json") { url.readText() } }
                .associateBy { info -> info.id }
            return LocalPluginGraph(infos)
        }
    }
}

/**
 * Initializes all the plugins that are present in the classpath.
 */
fun initializePluginsFromClasspath(context: Context, testing: Boolean = false) {
    for (impls in context[HextantClassLoader].getResources("implementations.json")) {
        val implementations: List<Implementation> =
            Json.tryParse("implementations.json") { impls.readText() } ?: emptyList()
        for (impl in implementations) {
            context[Aspects].addImplementation(impl, context[HextantClassLoader])
        }
    }
    val graph = LocalPluginGraph.load(context)
    val order = graph.topologicalSort() ?: error("cycle in dependencies")
    for (info in order) {
        val initializer = getInitializer(context, info)
        initializer?.tryApplyPhase(Enable, id = info.id, context = context, project = null, testing = testing)
        initializer?.tryApplyPhase(Initialize, id = info.id, context = context, project = null, testing = testing)
    }
}

internal fun PluginManager.autoLoadAndUnloadPluginsOnChange(context: Context, project: Editor<*>): Observer =
    enabledPlugins.observe { _, plugins ->
        runBlocking {
            addPlugins(plugins, context, Initialize, project = null)
            addPlugins(plugins, context, Initialize, project)
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
        initializer?.tryApplyPhase(Close, id = info.id, context = context, project = project)
        initializer?.tryApplyPhase(Close, id = info.id, context = context, project = null)
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

internal fun getProjectTypeInstance(cl: ClassLoader, clazz: String): ProjectType {
    val cls = cl.loadClass(clazz).kotlin
    val obj = cls.objectInstance
    val companion = cls.companionObjectInstance
    return obj as? ProjectType ?: companion as? ProjectType ?: Main.fail("Invalid project type $clazz")
}
