/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.*
import hextant.command.Commands
import hextant.command.line.*
import hextant.core.Editor
import hextant.core.InputMethod
import hextant.core.editor.getSimpleEditorConstructor
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.plugin.Aspects
import hextant.serial.BundleSerializer
import hextant.serial.SerialProperties
import hextant.settings.PropertyRegistrar
import hextant.undo.UndoManager
import kserial.KSerial
import kserial.SerialContext
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

    internal val classLoader = SimpleProperty<ClassLoader>("class loader")

    /**
     * Initialize some common properties on the project level.
     */
    fun initializeProjectContext(context: Context, loader: ClassLoader) = with(context) {
        set(Internal, Commands, Commands.newInstance())
        set(Internal, Inspections, Inspections.newInstance())
        set(Internal, Stylesheets, Stylesheets(loader))
        set(Internal, logger, Logger.getLogger(javaClass.name))
        set(Internal, propertyChangeHandlers, PropertyChangeHandlers())
        set(Internal, SerialProperties.serialContext, createSerialContext())
        set(Internal, SerialProperties.serial, KSerial.newInstance())
        set(PropertyRegistrar, PropertyRegistrar())
        set(Internal, Aspects, Aspects())
        set(Internal, classLoader, loader)
    }

    private fun createSerialContext(): SerialContext = SerialContext.newInstance {
        useUnsafe = true
        registerConstructor<Editor<*>> { bundle, cls ->
            cls.getSimpleEditorConstructor().invoke(bundle[SerialProperties.deserializationContext])
        }
        register(BundleSerializer)
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