/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.core.*
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.hextant.core.inspect.Inspections
import org.nikok.hextant.prop.PropertyHolder
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Logger

/**
 * The hextant platform, mainly functions as a [PropertyHolder] to manage properties of the hextant platform
 */
interface HextantPlatform : PropertyHolder {
    /**
     * Enqueues the specified [action] in the Hextant main thread
     */
    fun <T> runLater(action: () -> T): Future<T>

    /**
     * The default instance of the [HextantPlatform]
     */
    private class Impl(private val propertyHolder: PropertyHolder) : HextantPlatform, PropertyHolder by propertyHolder {
        private val executor = Executors.newSingleThreadExecutor()

        override fun <T> runLater(action: () -> T): Future<T> = executor.submit(action)
    }

    companion object {
        val defaultPropertyHolder = PropertyHolder.newInstance {
            set(Version, Version(1, 0, isSnapshot = true))
            set(SelectionDistributor, SelectionDistributor.newInstance())
            set(EditorViewFactory, EditorViewFactory.newInstance())
            set(EditableFactory, EditableFactory.newInstance(HextantPlatform::class.java.classLoader))
            set(Commands, Commands.newInstance())
            set(Inspections, Inspections.newInstance())
            val expanderFactory = ExpanderFactory.newInstance()
            set(ExpanderFactory, expanderFactory)
            set(EditorFactory, EditorFactory.newInstance(expanderFactory))
            set(CoreProperties.logger, Logger.getLogger("org.nikok.hextant"))
        }

        fun withPropertyHolder(propertyHolder: PropertyHolder): HextantPlatform = Impl(propertyHolder)

        val INSTANCE = HextantPlatform.withPropertyHolder(defaultPropertyHolder)
    }
}
