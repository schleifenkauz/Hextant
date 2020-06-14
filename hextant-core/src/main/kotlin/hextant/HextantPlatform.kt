/**
 *@author Nikolaus Knop
 */

package hextant

import hextant.command.Commands
import hextant.command.line.*
import hextant.config.EnabledSource
import hextant.core.*
import hextant.core.editor.getSimpleEditorConstructor
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.main.InputMethod
import hextant.plugin.impl.Plugins
import hextant.serial.SerialProperties
import hextant.serial.SerialProperties.deserializationContext
import hextant.settings.model.ConfigurableProperties
import hextant.undo.UndoManager
import kserial.KSerial
import kserial.SerialContext

/**
 * The hextant platform is responsible for creating the root context.
 */
object HextantPlatform {
    /**
     * Create the root context.
     */
    fun rootContext() = Context.newInstance {
        set(Internal, EditorControlFactory, EditorControlFactory.newInstance())
        set(Internal, EditorFactory, EditorFactory.newInstance())
        set(Internal, Commands, Commands.newInstance())
        set(Internal, Inspections, Inspections.newInstance())
        set(Internal, Stylesheets, Stylesheets())
        set(Plugins, Plugins(this))
        set(Internal, SerialProperties.serialContext, createSerialContext())
        set(Internal, SerialProperties.serial, KSerial.newInstance())
        set(Internal, ConfigurableProperties, ConfigurableProperties())
        val src = EnabledSource()
        src.addSource { get(Commands).all() }
        set(Internal, EnabledSource, src)
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
        set(CommandLine.forEditors, CommandLine(clContext, ContextCommandSource(this)))
        set(CommandLine.global, CommandLine(clContext, GlobalCommandSource(root)))
    }
}