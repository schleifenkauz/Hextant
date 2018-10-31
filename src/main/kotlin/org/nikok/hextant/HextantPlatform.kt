/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.core.*
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CoreProperties.logger
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.impl.SelectionDistributor
import org.nikok.hextant.core.inspect.Inspections
import org.nikok.hextant.prop.PropertyHolder
import org.nikok.hextant.prop.invoke
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
    companion object : HextantPlatform, PropertyHolder by PropertyHolder.newInstance() {
        private val executor = Executors.newSingleThreadExecutor()

        override fun <T> runLater(action: () -> T): Future<T> = executor.submit(action)

        init {
            Internal {
                HextantPlatform[Version] = Version(1, 0, isSnapshot = true)
                HextantPlatform[SelectionDistributor] = SelectionDistributor.newInstance()
                HextantPlatform[EditorViewFactory] = EditorViewFactory.newInstance()
                HextantPlatform[EditableFactory] = EditableFactory.newInstance()
                HextantPlatform[Commands] = Commands.newInstance()
                HextantPlatform[Inspections] = Inspections.newInstance()
                val expanderFactory = ExpanderFactory.newInstance()
                HextantPlatform[ExpanderFactory] = expanderFactory
                HextantPlatform[EditorFactory] = EditorFactory.newInstance(expanderFactory)
                HextantPlatform[logger] = Logger.getLogger("org.nikok.hextant")
            }
        }
    }
}
