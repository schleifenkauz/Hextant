/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.Bundle
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.*
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.hextant.core.inspect.Inspections
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
    private class Impl(bundle: Bundle) : HextantPlatform, Bundle by bundle {
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

        init {
            set(Version, Version(1, 0, isSnapshot = true))
            set(SelectionDistributor, SelectionDistributor.newInstance(this))
            val cl = this.javaClass.classLoader
            set(EditorViewFactory, EditorViewFactory.newInstance(this, cl))
            set(EditableFactory, EditableFactory.newInstance(cl))
            set(Commands, Commands.newInstance(this))
            set(Inspections, Inspections.newInstance(this))
            val expanderFactory = ExpanderFactory.newInstance(cl, this)
            set(ExpanderFactory, expanderFactory)
            set(EditorFactory, EditorFactory.newInstance(cl, this))
            set(CoreProperties.logger, Logger.getLogger("org.nikok.hextant"))
        }
    }

    companion object {
        val INSTANCE: HextantPlatform = newInstance()

        fun newInstance(bundle: Bundle = Bundle.newInstance()): HextantPlatform = Impl(bundle)
    }
}

@JvmName("getPublic")
operator fun <T : Any> HextantPlatform.get(property: Property<T, Public, *>): T = get(Public, property)

@JvmName("setPublic")
internal operator fun <T : Any> HextantPlatform.set(property: Property<T, *, Public>, value: T) =
    set(Public, property, value)

@JvmName("getInternal")
internal operator fun <T : Any> HextantPlatform.get(property: Property<T, Internal, *>): T = get(Internal, property)

@JvmName("setInternal")
internal operator fun <T : Any> HextantPlatform.set(property: Property<T, *, Internal>, value: T) =
    set(Internal, property, value)