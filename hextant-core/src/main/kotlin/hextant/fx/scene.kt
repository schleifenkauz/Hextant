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
        val node = ev.target as? Region ?: return@addEventFilter
        showCommandsPopup(node, ev)
    }
    registerShortcuts {
        on("Alt+Enter") { ev ->
            val node = ev.target as? Region ?: return@on
            showCommandsPopup(node, ev)
        }
    }
    if (applyStyle) context[Stylesheets].manage(this)
}

private fun showCommandsPopup(node: Region, ev: Event) {
    val control = editorControlInParentChain(node)
    if (control != null) {
        val p = node.localToScreen(0.0, node.height)
        val context = control.context
        val target  = control.target
        val onEditor = context[Commands].applicableOn(target)
        val onControl = context[Commands].applicableOn(control)
        val commands: List<Command<*, *>> = (onEditor + onControl).filter { cmd -> cmd.parameters.isEmpty() }
        if (commands.isEmpty()) return
        ev.consume()
        val list = CommandListView("Choose command", commands)
        val command = list.showPopup(p, owner = node.scene.window) ?: return
        command as Command<Any, *>
        when (command) {
            in onEditor -> command.execute(target, emptyList())
            in onControl -> command.execute(control, emptyList())
        }
    }
}

internal val Scene.focusedEditorControl: EditorControl<*>?
    get() = editorControlInParentChain(focusOwner)
