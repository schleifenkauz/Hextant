/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.PropertyChangeHandler
import bundles.property
import bundles.publicProperty
import bundles.set
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.command.line.CommandReceiverType
import hextant.command.line.ContextCommandSource
import hextant.config.FeatureRegistrar
import hextant.config.PropertyRegistrar
import hextant.fx.InputMethod
import hextant.fx.ResultStyleClasses
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.plugins.Aspects
import hextant.plugins.Marketplace
import hextant.undo.UndoManager
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

    val editorCommandLine = property<CommandLine, Internal>("editor-command-line")

    fun setupContext(context: Context) = with(context) {
        set(Internal, classLoader, javaClass.classLoader)
        set(Internal, Commands, Commands.newInstance())
        set(Internal, Inspections, Inspections.newInstance())
        set(Internal, Stylesheets, Stylesheets())
        set(Internal, logger, Logger.getLogger("Hextant Logger"))
        set(Internal, propertyChangeHandler, PropertyChangeHandler())
        set(Internal, Aspects, Aspects())
        set(PropertyRegistrar, PropertyRegistrar())
        set(ResultStyleClasses, ResultStyleClasses())
        set(FeatureRegistrar, FeatureRegistrar(this))
        set(SelectionDistributor, SelectionDistributor.newInstance())
        set(EditorControlGroup, EditorControlGroup())
        set(UndoManager, UndoManager.newInstance())
        set(Clipboard, SimpleClipboard())
        set(InputMethod, InputMethod.REGULAR)
        set(Internal, editorCommandLine, createCommandLine(ContextCommandSource(this, *CommandReceiverType.values())))
    }

    private fun Context.createCommandLine(source: ContextCommandSource): CommandLine {
        val commandLineContext = extend {
            set(SelectionDistributor, SelectionDistributor.newInstance())
        }
        return CommandLine(commandLineContext, source)
    }
}