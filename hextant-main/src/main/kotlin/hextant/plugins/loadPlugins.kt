package hextant.plugins

import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.core.Editor
import hextant.project.ProjectType
import kollektion.graph.topologicalSort
import kotlinx.coroutines.runBlocking
import reaktive.Observer
import kotlin.reflect.full.companionObjectInstance


fun addPlugins(plugins: Collection<Plugin>, context: Context, phase: PluginBuilder.Phase, project: Editor<*>?) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    for (plugin in order.reversed()) {
        val aspects = context[Aspects]
        if (project == null) {
            val cl = context[classLoader]
            cl.addPlugin(plugin.id, context)
            val impls = runBlocking { plugin.implementations.await() }
            for (impl in impls) {
                aspects.addImplementation(impl, cl)
            }
        }
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(info)
        initializer?.tryApplyPhase(phase, id = info.id, context = context, project = project)
    }
}

@JvmName("initializePluginsById")
fun addPlugins(pluginIds: Collection<String>, context: Context, phase: PluginBuilder.Phase, project: Editor<*>?) {
    val plugins = pluginIds.map { id -> context[PluginManager].getPlugin(id) }
    addPlugins(plugins, context, phase, project)
}

internal fun disablePlugins(plugins: Collection<Plugin>, context: Context, project: Editor<*>?) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    for (plugin in order.asReversed()) {
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(info) ?: continue
        initializer.tryApplyPhase(PluginBuilder.Phase.Disable, id = info.id, context = context, project = null)
        if (project != null)
            initializer.tryApplyPhase(PluginBuilder.Phase.Disable, id = info.id, context = context, project = project)
    }
}

private fun getInitializer(info: PluginInfo): PluginInitializer? {
    val initializer = getPluginInitializer(Thread.currentThread().contextClassLoader, info) ?: return null
    check(initializer is PluginInitializer) { "Unrecognized plugin initializer class: ${initializer::class}" }
    return initializer
}

fun PluginManager.autoLoadAndUnloadPluginsOnChange(context: Context, project: Editor<*>): Observer =
    enabledPlugins.observe { _, plugins ->
        runBlocking {
            addPlugins(plugins, context, PluginBuilder.Phase.Initialize, project = null)
            addPlugins(plugins, context, PluginBuilder.Phase.Initialize, project)
        }
    } and disabledPlugins.observe { _, plugins ->
        runBlocking { disablePlugins(plugins, context, project) }
    }

internal fun closeProject(context: Context) {
    val enabled = context[PluginManager].enabledPlugins()
    val project = context[Project].root
    for (plugin in enabled) {
        val info = runBlocking { plugin.info.await() }
        val initializer = getInitializer(info)
        initializer?.tryApplyPhase(PluginBuilder.Phase.Close, id = info.id, context = context, project = project)
        initializer?.tryApplyPhase(PluginBuilder.Phase.Close, id = info.id, context = context, project = null)
    }
}

fun getProjectTypeInstance(cl: ClassLoader, clazz: String): ProjectType {
    val cls = cl.loadClass(clazz).kotlin
    val obj = cls.objectInstance
    val companion = cls.companionObjectInstance
    return obj as? ProjectType ?: companion as? ProjectType ?: error("Invalid project type $clazz")
}
