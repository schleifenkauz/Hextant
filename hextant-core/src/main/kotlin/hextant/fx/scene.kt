/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.context.Context
import hextant.core.view.EditorControl
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
        val editorControl = editorControlInParentChain(node)
        if (editorControl != null) {
            val p = node.localToScreen(0.0, node.height)
            editorControl.commandsPopup.show(node, p.x, p.y)
            ev.consume()
        }
    }
    if (applyStyle) context[Stylesheets].manage(this)
}

internal val Scene.focusedEditorControl: EditorControl<*>?
    get() = editorControlInParentChain(focusOwner)
