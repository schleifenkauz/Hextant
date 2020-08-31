/**
 * @author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import hextant.context.Context
import hextant.context.createOutput
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import reaktive.Observer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

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
            aspects.addImplementation(impl, context[HextantClassLoader])
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
            context[Aspects].addImplementation(impl, context[HextantClassLoader])
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

internal data class Project(val root: Editor<*>, val context: Context, val location: Path) {
    fun save() {
        val output = context.createOutput(location.resolve(GlobalDirectory.PROJECT_ROOT))
        output.writeObject(root)
        val manager = context[PluginManager]
        val info = ProjectInfo(manager.enabledIds().toList(), manager.requiredPlugins)
        val txt = Json.encodeToString(info)
        Files.newBufferedWriter(location.resolve(GlobalDirectory.PROJECT_INFO)).use { w ->
            w.write(txt)
        }
    }

    companion object : SimpleProperty<Project>("project")
}

internal fun Aspects.addImplementation(implementation: Implementation, classLoader: ClassLoader) {
    @Suppress("UNCHECKED_CAST")
    val aspect = classLoader.loadClass(implementation.aspect).kotlin as KClass<Any>
    val case = classLoader.loadClass(implementation.feature).kotlin
    val impl = classLoader.loadClass(implementation.clazz).kotlin
    check(impl.isSubclassOf(aspect)) { "invalid implementation class $impl for aspect $aspect" }
    val instance = impl.objectInstance ?: impl.createInstance()
    implement(aspect, case, instance)
}