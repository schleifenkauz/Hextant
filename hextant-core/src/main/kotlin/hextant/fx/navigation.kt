package hextant.fx

import fxutils.shortcut
import hextant.core.view.EditorControl
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.KeyCode.TAB
import javafx.scene.input.KeyEvent

private val TRAV_NEXT = "Ctrl?+TAB".shortcut
private val TRAV_PREV = "Ctrl?+Shift + TAB".shortcut

internal fun Scene.selectNext(travelToLeaf: Boolean): Boolean {
    val control = editorControlInParentChain(focusOwner) ?: return false
    var next = control.next() ?: return false
    if (travelToLeaf) {
        while (next.editorChildren().isNotEmpty()) {
            next = next.editorChildren().first()
        }
    }
    next.select()
    return true
}

internal fun Scene.selectPrevious(travelToLeaf: Boolean): Boolean {
    val control = editorControlInParentChain(focusOwner) ?: return false
    var previous = control.previous() ?: return false
    if (travelToLeaf) {
        while (previous.editorChildren().isNotEmpty()) {
            previous = previous.editorChildren().last()
        }
    }
    previous.select()
    return true
}


internal fun editorControlInParentChain(node: Node) =
    generateSequence(node) { it.parent }.firstOrNull { it is EditorControl<*> } as EditorControl<*>?

internal fun Scene.registerNavigationShortcuts() {
    ModifierKeyTracker.start()
    addEventFilter(KeyEvent.ANY) { ev ->
        if (ev.code == TAB) {
            ev.consume()
            if (ev.eventType != KeyEvent.KEY_RELEASED) return@addEventFilter
            if (TRAV_NEXT.matches(ev)) selectNext(travelToLeaf = !ev.isControlDown)
            else if (TRAV_PREV.matches(ev)) selectPrevious(travelToLeaf = !ev.isControlDown)
        }
    }
}
