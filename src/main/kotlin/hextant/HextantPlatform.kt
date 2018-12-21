/**
 *@author Nikolaus Knop
 */

package hextant

import hextant.bundle.Bundle
import hextant.bundle.CoreProperties
import hextant.command.Commands
import hextant.core.*
import hextant.impl.SelectionDistributor
import hextant.impl.Settings
import hextant.inspect.Inspections
import hextant.plugin.PluginRegistry
import kserial.SerialContext
import java.util.concurrent.*
import java.util.logging.Logger

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
     * The default instance of the [HextantPlatform]
     */
    private class Impl(bundle: Bundle) : HextantPlatform, AbstractContext(null, bundle) {
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
            set(CoreProperties.logger, Logger.getLogger(javaClass.name))
            val plugins = PluginRegistry(this, Settings.plugins)
            set(PluginRegistry, plugins)
            set(CoreProperties.serialContext, SerialContext.newInstance {
                classLoader = plugins.compoundClassLoader
                useUnsafe = false
            })
            set(CoreProperties.classLoader, plugins.compoundClassLoader)
        }

        fun unconfigured(bundle: Bundle = Bundle.newInstance()): HextantPlatform = Impl(bundle)
    }
}