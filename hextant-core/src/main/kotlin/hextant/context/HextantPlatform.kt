/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.Property
import bundles.PropertyChangeHandlers
import hextant.command.Commands
import hextant.command.line.*
import hextant.core.Editor
import hextant.core.editor.getSimpleEditorConstructor
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.main.InputMethod
import hextant.serial.SerialProperties
import hextant.serial.SerialProperties.deserializationContext
import hextant.settings.model.ConfigurableProperties
import hextant.undo.UndoManager
import kserial.KSerial
import kserial.SerialContext
import java.util.logging.Logger

/**
 * The hextant platform is responsible for creating the root context.
 */
object HextantPlatform {
    /**
     * The logger property
     */
    val logger = Property<Logger, Any, Internal>("top level logger")


    /**
     * The [PropertyChangeHandlers] is used to register properties for views.
     */
    val propertyChangeHandlers =
        Property<PropertyChangeHandlers, Any, Internal>("property change handlers")

    /**
     * Create the root context.
     */
    fun rootContext() = Context.newInstance {
        set(Internal, EditorControlFactory, EditorControlFactory.newInstance())
        set(Internal, EditorFactory, EditorFactory.newInstance())
        set(Internal, Commands, Commands.newInstance())
        set(Internal, Inspections, Inspections.newInstance())
        set(Internal, Stylesheets, Stylesheets())
        set(Internal, logger, Logger.getLogger(javaClass.name))
        set(Internal, propertyChangeHandlers, PropertyChangeHandlers())
        set(Internal, SerialProperties.serialContext, createSerialContext())
        set(Internal, SerialProperties.serial, KSerial.newInstance())
        set(Internal, ConfigurableProperties, ConfigurableProperties())
    }

    /**
     * Create the serial context.
     */
    fun createSerialContext(): SerialContext = SerialContext.newInstance {
        useUnsafe = true
        registerConstructor<Editor<*>> { bundle, cls ->
            cls.getSimpleEditorConstructor().invoke(bundle[deserializationContext])
        }
    }

    /**
     * Extend the [rootContext] with some core properties.
     */
    fun defaultContext(root: Context) = root.extend {
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