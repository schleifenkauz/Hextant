package hextant.plugins

import hextant.context.Context
import hextant.context.Properties.classLoader
import hextant.context.Properties.marketplace
import hextant.core.Editor
import hextant.cli.HextantDirectory
import hextant.cli.HextantDirectory.PLUGIN_CACHE
import hextant.cli.fail
import hextant.main.Project
import hextant.plugins.PluginBuilder.Phase.*
import javafx.application.Application
import kollektion.graph.topologicalSort
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import reaktive.Observer

internal enum class PluginSource {
    Classpath, ProjectInfo;

    companion object {
        private lateinit var value: PluginSource

        fun dynamic(): Boolean = value == ProjectInfo

        fun fromParameters(parameters: Application.Parameters) {
            value = when (val src = parameters.named["plugin-source"]) {
                null, "project-info" -> ProjectInfo
                "classpath" -> Classpath
                else -> fail("Unrecognized plugin source '$src'")
            }
        }
    }
}

private val loadedPluginIds = mutableSetOf<String>()

private fun ClassLoader.addURL(path: String) {
    val field = javaClass.getDeclaredField("ucp")
    field.isAccessible = true
    val ucp = field.get(this)
    val method = ucp.javaClass.getMethod("addFile", String::class.java)
    method.isAccessible = true
    method.invoke(ucp, path)
}

internal fun ClassLoader.addPluginsToClasspath(pluginIds: List<String>) {
    for (id in pluginIds) {
        if (id in loadedPluginIds) continue
        val file = HextantDirectory[PLUGIN_CACHE].resolve("$id.jar")
        addURL(file.toString())
        loadedPluginIds.add(id)
    }
}

internal fun registerImplementations(pluginIds: Collection<String>, context: Context) {
    val aspects = context[Aspects]
    for (id in pluginIds) {
        val implementations = runBlocking { context[marketplace].get(PluginProperty.implementations, id) }.orEmpty()
        for (impl in implementations) {
            aspects.addImplementation(impl, context[classLoader])
        }
    }
}

internal fun applyPhase(
    phase: PluginBuilder.Phase,
    plugins: Collection<Plugin>,
    context: Context,
    project: Editor<*>?
) {
    val order = PluginGraph(context[PluginManager], plugins).topologicalSort() ?: error("cycle in dependencies")
    val infos = runBlocking { order.map { plugin -> plugin.info.await() } }
    applyPhase(phase, infos, context, project)
}

internal fun applyPhase(
    phase: PluginBuilder.Phase,
    infos: List<PluginInfo>,
    context: Context,
    project: Editor<*>?
) {
    for (plugin in infos) {
        val initializer = getPluginInitializer(context[classLoader], plugin) ?: continue
        check(initializer is PluginInitializer) { "Unrecognized plugin initializer class: ${initializer::class}" }
        try {
            initializer.apply(context, phase, project, false)
        } catch (ex: Throwable) {
            System.err.println("Error while applying $phase to plugin ${plugin.id}")
            ex.printStackTrace()
        }
    }
}

internal fun Project.listenForPluginChanges(): Observer {
    val manager = context[PluginManager]
    return manager.enabledPlugins.observe { _, plugins ->
        val pluginIds = plugins.map { plugin -> plugin.id }
        context[classLoader].addPluginsToClasspath(pluginIds)
        registerImplementations(pluginIds, context)
        applyPhase(Enable, plugins, context, project = null)
        applyPhase(Initialize, plugins, context, project = null)
        applyPhase(Enable, plugins, context, root)
        applyPhase(Initialize, plugins, context, root)
    } and manager.disabledPlugins.observe { _, plugins ->
        applyPhase(Disable, plugins, context, project = null)
        applyPhase(Disable, plugins, context, root)
    }
}

internal fun loadPluginInfosFromClasspath(context: Context): List<PluginInfo> =
    context[classLoader].getResources("plugin.json").toList().map { info ->
        Json.decodeFromString(info.readText())
    }

internal fun registerImplementationsFromClasspath(context: Context) {
    val cl = context[classLoader]
    for (impls in cl.getResources("implementations.json")) {
        val implementations: List<Implementation> = Json.decodeFromString(impls.readText())
        for (impl in implementations) {
            context[Aspects].addImplementation(impl, cl)
        }
    }
}
