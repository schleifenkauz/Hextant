package hextant.core

import bundles.PropertyChangeHandler
import bundles.set
import hextant.command.Command.Type.SingleReceiver
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.command.line.CommandReceiverType.*
import hextant.command.line.ContextCommandSource
import hextant.command.line.SingleCommandSource
import hextant.config.FeatureRegistrar
import hextant.config.PropertyRegistrar
import hextant.config.disable
import hextant.config.enable
import hextant.context.*
import hextant.context.Properties.classLoader
import hextant.context.Properties.globalCommandLine
import hextant.context.Properties.localCommandLine
import hextant.context.Properties.logger
import hextant.context.Properties.propertyChangeHandler
import hextant.fx.InputMethod
import hextant.fx.ResultStyleClasses
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.plugins.*
import hextant.undo.UndoManager
import reaktive.value.binding.flatMap
import reaktive.value.now
import reaktive.value.reactiveValue
import java.util.logging.Logger

/**
 * The core plugin registers basic editors, views and commands.
 */
object HextantCore : PluginInitializer({
    /*persistentProperty(Internal, Settings)*/
    stylesheet("hextant/core/style.css")
    registerCommand(enable)
    registerCommand(disable)
    registerCommand<Editor<*>, String> {
        name = "Undo"
        shortName = "undo"
        description = "Undoes the last edit"
        type = SingleReceiver
        defaultShortcut("Alt?+Z")
        applicableIf { e -> e.context.hasProperty(UndoManager) && e.context[UndoManager].canUndo }
        executing { editor, _ ->
            val m = editor.context[UndoManager]
            val description = m.undoText
            m.undo()
            description
        }
    }
    registerCommand<Editor<*>, String> {
        name = "Redo"
        shortName = "redo"
        description = "Redoes the last undone edit"
        type = SingleReceiver
        defaultShortcut("Alt?+Y")
        applicableIf { e -> e.context.hasProperty(UndoManager) && e.context[UndoManager].canRedo }
        executing { editor, _ ->
            val m = editor.context[UndoManager]
            val description = m.redoText
            m.redo()
            description
        }
    }
    registerInspection<CommandLine> {
        id = "unavailable-command"
        description = "Reports commands that are not available in the current context"
        isSevere(true)
        checkingThat {
            inspected.expandedCommand.flatMap { c ->
                c?.let { inspected.source.isApplicable(c) } ?: reactiveValue(true)
            }
        }
        message {
            val name = inspected.expandedCommand.now!!.name
            "Command '$name' is not available in the current context"
        }
    }
}) {
    fun defaultContext() = Context.create {
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
        set(Internal, localCommandLine, CommandLine.create(this, ContextCommandSource(this, Targets, Expanders, Views)))
        set(Internal, globalCommandLine, CommandLine.create(this, SingleCommandSource(this, this)))
    }
}