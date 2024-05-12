/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.PropertyChangeHandler
import bundles.property
import bundles.publicProperty
import hextant.command.line.CommandLine
import hextant.plugins.Marketplace
import java.util.logging.Logger

/**
 * The hextant platform is responsible for creating the root context.
 */
object Properties {
    /**
     * The logger property
     */
    val logger = property<Logger, Internal>("top level logger")


    /**
     * The [PropertyChangeHandler] is used to register properties for views.
     */
    val propertyChangeHandler = property<PropertyChangeHandler, Internal>("property change handlers")

    /**
     * The class loader to be used by all plugins.
     */
    val classLoader = property<ClassLoader, Internal>("class loader")

    val marketplace = publicProperty<Marketplace>("marketplace")

    val localCommandLine = property<CommandLine, Internal>("editor-command-line")

    val globalCommandLine = property<CommandLine, Internal>("context-command-line")
}