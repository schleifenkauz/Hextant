/**
 *@author Nikolaus Knop
 */

package hextant

import hextant.bundle.Internal
import hextant.command.Commands
import hextant.impl.SelectionDistributor
import hextant.impl.Stylesheets
import hextant.inspect.Inspections
import hextant.main.InputMethod
import hextant.plugin.impl.Plugins
import hextant.serial.SerialProperties
import hextant.settings.model.ConfigurableProperties
import hextant.undo.UndoManager
import kserial.KSerial
import kserial.SerialContext

object HextantPlatform {
    fun rootContext() = Context.newInstance {
        set(Internal, EditorControlFactory, EditorControlFactory.newInstance())
        set(Internal, EditorFactory, EditorFactory.newInstance())
        set(Internal, Commands, Commands.newInstance())
        set(Internal, Inspections, Inspections.newInstance())
        set(Stylesheets, Stylesheets())
        set(Plugins, Plugins(this))
        set(
            Internal,
            SerialProperties.serialContext,
            SerialContext(classLoader = HextantPlatform.javaClass.classLoader)
        )
        set(Internal, SerialProperties.serial, KSerial.newInstance())
        set(Internal, ConfigurableProperties, ConfigurableProperties())
    }

    fun defaultContext(root: Context) = root.extend {
        set(SelectionDistributor, SelectionDistributor.newInstance())
        set(EditorControlGroup, EditorControlGroup())
        set(UndoManager, UndoManager.newInstance())
        set(InputMethod, InputMethod.REGULAR)
    }
}