/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.core.Editor
import hextant.main.plugins.PluginGraph
import hextant.main.plugins.PluginManager
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase
import hextant.plugin.PluginBuilder.Phase.*
import hextant.plugin.PluginInitializer
import hextant.plugins.*
import hextant.project.ProjectType
import kollektion.graph.topologicalSort
import kotlinx.coroutines.runBlocking
import reaktive.Observer
import kotlin.reflect.full.companionObjectInstance

internal fun addPlugins(plugins: Collection<Plugin>, context: Context, phase: Phase, project: Editor<*>?) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    for (plugin in order) {
        val aspects = context[Aspects]
        if (project == null) {
            val cl = context[classLoader] as HextantClassLoader
            cl.addPlugin(plugin.id)
            val impls = runBlocking { plugin.implementations.await() }
            for (impl in impls) {
                aspects.addImplementation(impl, cl)
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

private fun getInitializer(context: Context, info: PluginInfo): PluginInitializer? {
    val initializer = getInitializer(context[classLoader], info) ?: return null
    check(initializer is PluginInitializer) { "Unrecognized plugin initializer class: ${initializer::class}" }
    return initializer
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

internal fun getProjectTypeInstance(cl: ClassLoader, clazz: String): ProjectType {
    val cls = cl.loadClass(clazz).kotlin
    val obj = cls.objectInstance
    val companion = cls.companionObjectInstance
    return obj as? ProjectType ?: companion as? ProjectType ?: Main.fail("Invalid project type $clazz")
}
