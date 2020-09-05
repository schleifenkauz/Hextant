package hextant.core

import hextant.command.Command.Type.SingleReceiver
import hextant.command.line.CommandLine
import hextant.config.disable
import hextant.config.enable
import hextant.context.Internal
import hextant.core.editor.ValidatedTokenEditor
import hextant.plugin.*
import hextant.settings.Settings
import hextant.undo.UndoManager
import reaktive.value.binding.flatMap
import reaktive.value.binding.map
import reaktive.value.now
import reaktive.value.reactiveValue
import validated.Validated.Invalid

/**
 * The core plugin registers basic editors, views and commands.
 */
object Core : PluginInitializer({
    persistentProperty(Internal, Settings)
    registerInspection<Editor<*>> {
        id = "syntax-error"
        description = "Highlights syntax errors"
        isSevere(true)
        checkingThat { inspected.result.map { it !is Invalid } }
        message {
            val result = inspected.result.now as Invalid
            result.reason
        }
    }
    registerInspection<ValidatedTokenEditor<*>> {
        id = "invalid-intermediate"
        description = "Reports invalid intermediate result"
        checkingThat { inspected.intermediateResult.map { it !is Invalid } }
        message { (inspected.intermediateResult.now as Invalid).reason }
        isSevere(true)
    }
    stylesheet("hextant/core/style.css")
    command(enable)
    command(disable)
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
})