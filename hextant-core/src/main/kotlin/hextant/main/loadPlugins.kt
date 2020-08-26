/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.Context
import hextant.core.Editor
import hextant.main.HextantApp.Companion.marketplace
import hextant.plugin.Aspects
import hextant.plugin.PluginBuilder.Phase
import hextant.plugin.PluginBuilder.Phase.Disable
import hextant.plugin.PluginInitializer
import hextant.plugins.getImplementations
import hextant.plugins.getPluginById
import kotlin.reflect.full.createInstance

internal fun loadPlugins(plugins: List<String>, context: Context, phase: Phase, project: Editor<*>?) {
    for (id in plugins) {
        loadPlugin(id, context, phase, project)
    }
}

internal fun loadPlugin(
    id: String,
    context: Context,
    phase: Phase,
    project: Editor<*>?
) {
    val marketplace = context[marketplace]
    val aspects = context[Aspects]
    if (project == null) {
        val cl = Thread.currentThread().contextClassLoader as HextantClassLoader
        cl.addPlugin(id)
        val impls = marketplace.getImplementations(id).orEmpty()
        for (impl in impls) {
            aspects.addImplementation(impl)
        }
    }
    val initializer = getInitializer(context, id)
    initializer.apply(context, phase, project)
}

private fun getInitializer(context: Context, id: String): PluginInitializer {
    val plugin = context[marketplace].getPluginById(id) ?: error("Unknown plugin '$id'")
    val cls = Class.forName(plugin.initializer).kotlin
    val initializer = cls.objectInstance ?: cls.createInstance()
    check(initializer is PluginInitializer) { "Invalid initializer $cls" }
    return initializer
}

internal fun disablePlugin(id: String, context: Context, project: Editor<*>) {
    val initializer = getInitializer(context, id)
    initializer.apply(context, Disable, project = null)
    initializer.apply(context, Disable, project)
}
