package hextant.fx

import hextant.core.view.EditorControl
import hextant.core.view.ExpanderControl
import hextant.impl.iterate
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.KeyCode.SHIFT
import javafx.scene.input.KeyEvent
import reaktive.value.now

private val TRAV_NEXT = "TAB".shortcut
private val TRAV_PREV = "Shift + TAB".shortcut
private val SELECT_PREV = "Ctrl + Left".shortcut
private val MOVE_PREV = "Ctrl + Shift + Left".shortcut
private val SELECT_NEXT = "Ctrl + Right".shortcut
private val MOVE_NEXT = "Ctrl + Shift + Right".shortcut
private fun Scene.moveNext() {
    val focused = focusedEditorControl ?: return
    val next = focused.nextEditorControl ?: return
    if (next.isSelected.now) {
        focused.toggleSelection()
        next.justFocus()
    } else {
        next.toggleSelection()
    }
}

private fun Scene.movePrev() {
    val focused = focusedEditorControl ?: return
    val prev = focused.previousEditorControl ?: return
    if (prev.isSelected.now) {
        focused.toggleSelection()
        prev.justFocus()
    } else {
        prev.toggleSelection()
    }
}

internal fun Scene.selectNext(): Boolean {
    val next = focusOwner.nextEditorControl
    next?.select()
    return true
}

internal fun Scene.selectPrevious(): Boolean {
    val prev = focusOwner.previousEditorControl
    prev?.focus()
    return prev != null
}

private val Node.previousEditorControl
    get() = iterate(editorControlInParentChain(this)?.previous()) {
        if (it is ExpanderControl) it.root as? EditorControl<*>
        else it.editorChildren().lastOrNull()
    }

private val Node.nextEditorControl
    get() = iterate(editorControlInParentChain(this)?.next()) {
        if (it is ExpanderControl) it.root as? EditorControl<*>
        else it.editorChildren().firstOrNull()
    }


internal fun editorControlInParentChain(node: Node) =
    generateSequence(node) { it.parent }.firstOrNull { it is EditorControl<*> } as EditorControl<*>?

internal var isShiftDown = false; private set

private fun Scene.listenForShift() {
    addEventFilter(KeyEvent.KEY_PRESSED) {
        if (it.code == SHIFT) {
            isShiftDown = true
        }
    }
    addEventFilter(KeyEvent.KEY_RELEASED) { ev ->
        if (ev.code == SHIFT) {
            isShiftDown = false
        }
    }
}

internal fun Scene.registerNavigationShortcuts() {
    listenForShift()
    registerShortcuts {
        on(SELECT_PREV) {
            selectPrevious()
        }
        on(MOVE_PREV) {
            movePrev()
        }
        on(SELECT_NEXT) {
            selectNext()
        }
        on(MOVE_NEXT) {
            moveNext()
        }
    }
    registerShortcuts(KeyEvent.ANY) {
        on(TRAV_NEXT) {
            if (it.eventType == KeyEvent.KEY_RELEASED) focusedEditorControl?.next()?.select()
        }
        on(TRAV_PREV) {
            if (it.eventType == KeyEvent.KEY_RELEASED) focusedEditorControl?.previous()?.select()
        }
    }
}