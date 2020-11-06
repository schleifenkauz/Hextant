/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.Property
import bundles.PropertyChangeHandlers
import hextant.command.Commands
import hextant.command.line.*
import hextant.fx.InputMethod
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.plugin.Aspects
import hextant.settings.PropertyRegistrar
import hextant.undo.UndoManager
import java.util.logging.Logger

/**
 * The hextant platform is responsible for creating the root context.
 */
object Properties {
    /**
     * The logger property
     */
    val logger = Property<Logger, Any, Internal>("top level logger")


    /**
     * The [PropertyChangeHandlers] is used to register properties for views.
     */
    val propertyChangeHandlers = Property<PropertyChangeHandlers, Any, Internal>("property change handlers")

    /**
     * The class loader to be used by all plugins.
     */
    val classLoader = Property<ClassLoader, Any, Internal>("class loader")

    /**
     * Initialize some common properties on the project level.
     */
    fun projectContext(parent: Context, loader: ClassLoader) =
        parent.extend {
            set(Internal, Commands, Commands.newInstance())
            set(Internal, Inspections, Inspections.newInstance())
            set(Internal, Stylesheets, Stylesheets(loader))
            set(Internal, logger, Logger.getLogger(javaClass.name))
            set(Internal, propertyChangeHandlers, PropertyChangeHandlers())
            set(PropertyRegistrar, PropertyRegistrar())
            set(Internal, Aspects, Aspects())
            set(Internal, classLoader, loader)
        }

    /**
     * Extend the given [context] with some core properties.
     */
    fun defaultContext(context: Context) = context.extend {
        set(SelectionDistributor, SelectionDistributor.newInstance())
        set(EditorControlGroup, EditorControlGroup())
        set(UndoManager, UndoManager.newInstance())
        set(Clipboard, SimpleClipboard())
        set(InputMethod, InputMethod.REGULAR)
        val clContext = extend {
            set(SelectionDistributor, SelectionDistributor.newInstance())
        }
        set(CommandLine, CommandLine(clContext, ContextCommandSource(this, *CommandReceiverType.values())))
    }
}