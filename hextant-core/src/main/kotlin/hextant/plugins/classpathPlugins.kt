/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import hextant.context.Context
import hextant.plugins.PluginBuilder.Phase.Enable
import hextant.plugins.PluginBuilder.Phase.Initialize
import kollektion.graph.topologicalSort
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Initializes all the plugins that are present in the classpath.
 */
fun initializePluginsFromClasspath(
    context: Context,
    classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
    testing: Boolean = false
) {
    val cl = Thread.currentThread().contextClassLoader
    for (impls in cl.getResources("implementations.json")) {
        val implementations: List<Implementation> = Json.decodeFromString(impls.readText())
        for (impl in implementations) {
            context[Aspects].addImplementation(impl, cl)
        }
    }
    val graph = LocalPluginGraph.load(cl)
    val order = graph.topologicalSort() ?: error("cycle in dependencies")
    for (info in order) {
        val initializer = getPluginInitializer(classLoader, info) ?: continue
        check(initializer is PluginInitializer) { "Unrecognized plugin initializer class: ${initializer::class}" }
        initializer.apply(context, Enable, project = null, testing)
        initializer.apply(context, Initialize, project = null, testing)
    }
}

