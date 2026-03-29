/**
 * @author Nikolaus Knop
 */

package hextant.fx

import fxutils.registerShortcuts
import hextant.command.Command
import hextant.command.Commands
import hextant.command.line.CommandListView
import hextant.context.Context
import hextant.core.view.EditorControl
import hextant.inspect.Inspections
import javafx.event.Event
import javafx.scene.Scene
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.Region

/**
 * Initializes this scene with the given [context] by registering top level shortcuts and applying registered stylesheets.
 */
fun Scene.initHextantScene(context: Context, applyStyle: Boolean = true) {
    registerNavigationShortcuts()
    registerCopyPasteShortcuts(context)
    addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED) { ev ->
        val control = ev.getTargetEditorControl() ?: return@addEventFilter
        showCommandsPopup(control, ev)
    }
    registerShortcuts {
        on("Alt+Enter") { ev ->
            val control = ev.getTargetEditorControl() ?: return@on
            val problems = context[Inspections].getProblems(control.target)
            if (problems.isNotEmpty()) {
                control.showInspections()
            } else {
                showCommandsPopup(control, ev)
            }
        }
        on("Ctrl+L") { ev ->
            val control = ev.getTargetEditorControl() ?: return@on
            control.shrinkSelection()
            ev.consume()
        }
        on("Ctrl+M") { ev ->
            val control = ev.getTargetEditorControl() ?: return@on
            control.extendSelection()
            ev.consume()
        }
    }
    if (applyStyle) context[Stylesheets].manage(this)
}

private fun Event.getTargetEditorControl(): EditorControl<*>? {
    val node = target as? Region ?: return null
    return editorControlInParentChain(node)
}

private fun showCommandsPopup(control: EditorControl<*>, ev: Event) {
    val p = control.localToScreen(0.0, control.height)
    val context = control.context
    val target  = control.target
    val onEditor = context[Commands].applicableOn(target)
    val onControl = context[Commands].applicableOn(control)
    val commands: List<Command<*, *>> = (onEditor + onControl).filter { cmd -> cmd.parameters.isEmpty() }
    if (commands.isEmpty()) return
    ev.consume()
    val list = CommandListView("Choose command", commands)
    val command = list.showPopup(p, owner = control.scene.window) ?: return
    command as Command<Any, *>
    when (command) {
        in onEditor -> command.execute(target, emptyList())
        in onControl -> command.execute(control, emptyList())
    }
}

internal val Scene.focusedEditorControl: EditorControl<*>?
    get() = editorControlInParentChain(focusOwner)
