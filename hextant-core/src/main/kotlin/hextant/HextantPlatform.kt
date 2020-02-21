/**
 *@author Nikolaus Knop
 */

package hextant

import hextant.bundle.Internal
import hextant.command.Commands
import hextant.impl.*
import hextant.inspect.Inspections
import hextant.main.InputMethod
import hextant.plugin.PluginRegistry
import hextant.serial.HextantSerialContext
import hextant.serial.SerialProperties
import hextant.undo.UndoManager
import kserial.KSerial

object HextantPlatform {
    fun rootContext() = Context.newInstance {
        set(Internal, EditorControlFactory, EditorControlFactory.newInstance())
        set(Internal, EditorFactory, EditorFactory.newInstance())
        set(Internal, Commands, Commands.newInstance())
        set(Internal, Inspections, Inspections.newInstance())
        set(Internal, Stylesheets, Stylesheets())
        val plugins = PluginRegistry(this, Settings.plugins)
        set(Internal, PluginRegistry, plugins)
        set(Internal, SerialProperties.serialContext, HextantSerialContext(this, plugins.compoundClassLoader))
        set(Internal, SerialProperties.serial, KSerial.newInstance())
    }

    fun defaultContext(root: Context) = root.extend {
        set(SelectionDistributor, SelectionDistributor.newInstance())
        set(EditorControlGroup, EditorControlGroup())
        set(UndoManager, UndoManager.newInstance())
        set(InputMethod, InputMethod.REGULAR)
    }
}