/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.Bundle
import org.nikok.hextant.core.*
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.hextant.core.inspect.Inspections
import org.nikok.hextant.plugin.Plugin
import org.nikok.hextant.plugin.PluginLoader
import java.util.concurrent.*
import java.util.logging.Logger
import javax.json.stream.JsonParser

/**
 * The hextant platform, mainly functions as a [Bundle] to manage properties of the hextant platform
 */
interface HextantPlatform : Context {
    /**
     * Enqueues the specified [action] in the Hextant main thread
     */
    fun <T> runLater(action: () -> T): Future<T>

    fun exit()

    /**
     * Loads a plugin from the specified [json]-reader
     * @throws org.nikok.hextant.plugin.PluginException if an exception occurs during loading
     */
    fun loadPlugin(json: JsonParser)

    /**
     * Returns the [Plugin] instance for the plugin with the specified [name] previously load by [loadPlugin]
     */
    fun getPlugin(name: String): Plugin

    /**
     * The default instance of the [HextantPlatform]
     */
    private class Impl(bundle: Bundle) : HextantPlatform, AbstractContext(null, bundle) {
        private val plugins = mutableMapOf<String, Plugin>()

        private val executor = Executors.newSingleThreadExecutor()

        override fun <T> runLater(action: () -> T): Future<T> {
            val future = executor.submit(action)
            return CompletableFuture.supplyAsync { future.get() }.exceptionally { it.printStackTrace(); throw it }
        }

        override fun exit() {
            executor.shutdown()
        }

        override val platform: HextantPlatform
            get() = this

        override fun loadPlugin(json: JsonParser) {
            val loader = PluginLoader(json, platform, ClassLoader.getSystemClassLoader())
            val p = loader.load()
            plugins[p.name] = p
        }

        override fun getPlugin(name: String): Plugin =
            plugins[name] ?: throw NoSuchElementException("No plugin with name $name loaded")
    }

    companion object {
        val INSTANCE: HextantPlatform = configured()

        fun configured(bundle: Bundle = Bundle.newInstance()): HextantPlatform =
            unconfigured(bundle).apply { configure() }

        private fun HextantPlatform.configure() {
            set(Version, Version(1, 0, isSnapshot = true))
            set(SelectionDistributor, SelectionDistributor.newInstance())
            set(EditorControlFactory, EditorControlFactory.newInstance())
            set(EditableFactory, EditableFactory.newInstance())
            set(Commands, Commands.newInstance())
            set(Inspections, Inspections.newInstance())
            set(EditorFactory, EditorFactory.newInstance())
            set(CoreProperties.logger, Logger.getLogger("org.nikok.hextant"))
        }

        fun unconfigured(bundle: Bundle = Bundle.newInstance()): HextantPlatform = Impl(bundle)
    }
}