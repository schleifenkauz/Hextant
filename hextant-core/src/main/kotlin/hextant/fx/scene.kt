/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.*
import hextant.core.view.FXExpanderView
import hextant.impl.*
import javafx.scene.*
import javafx.scene.input.KeyCode.SHIFT
import javafx.scene.input.KeyEvent
import reaktive.value.now

internal fun hextantScene(
    root: (Context) -> Parent,
    createContext: (Context) -> Context
): Scene {
    val rootCtx = HextantPlatform.rootContext()
    val context = createContext(rootCtx)
    val scene = Scene(root(context))
    scene.initHextantScene(context)
    return scene
}

/**
 * Initializes this scene with the given [context] by registering top level shortcuts and applying registered stylesheets.
 */
fun Scene.initHextantScene(context: Context) {
    listenForShift()
    registerShortcuts(context)
    context[Internal, Stylesheets].apply(this)
}

private val TRAV_NEXT = "TAB".shortcut
private val TRAV_PREV = "Shift + TAB".shortcut
private val SELECT_PREV = "Ctrl + Left".shortcut
private val MOVE_PREV = "Ctrl + Shift + Left".shortcut
private val SELECT_NEXT = "Ctrl + Right".shortcut
private val MOVE_NEXT = "Ctrl + Shift + Right".shortcut
private val COPY = "Ctrl + C".shortcut
private val COPY_MANY = "Ctrl + Shift + C".shortcut

private val PASTE = "Ctrl + V".shortcut

private fun Scene.registerShortcuts(context: Context) {
    registerShortcuts {
        on(COPY) {
            copyToClipboard()
        }
        on(COPY_MANY) {
            copyManyToClipboard(context)
        }
        on(PASTE) {
            pasteFromClipboard()
        }
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
            if (it.eventType == KeyEvent.KEY_RELEASED) focusedEditorControl?.next?.select()
        }
        on(TRAV_PREV) {
            if (it.eventType == KeyEvent.KEY_RELEASED) focusedEditorControl?.previous?.select()
        }
    }
}

private fun Scene.pasteFromClipboard() {
    val view = focusedEditorControl
    val editor = view?.target
    if (editor is Editor<*>) {
        val success = editor.pasteFromClipboard()
        if (!success) editor.expander?.pasteFromClipboard()
    }
}

private fun copyManyToClipboard(context: Context) {
    val selected = context[SelectionDistributor].selectedTargets.now
    if (selected.any { it !is Editor<*> }) return
    val snapshots = selected.map { (it as Editor<*>).createSnapshot() }
    context[Internal, CoreProperties.clipboard] = ClipboardContent.MultipleEditors(snapshots)
}

private fun Scene.copyToClipboard() {
    val view = focusedEditorControl
    val editor = view?.target
    if (editor is Editor<*>) editor.copyToClipboard()
}

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
    get() = iterate(editorControlInParentChain(this)?.previous) {
        if (it is FXExpanderView) it.root as? EditorControl<*>
        else it.editorChildren().lastOrNull()
    }

private val Node.nextEditorControl
    get() = iterate(editorControlInParentChain(this)?.next) {
        if (it is FXExpanderView) it.root as? EditorControl<*>
        else it.editorChildren().firstOrNull()
    }

private val Scene.focusedEditorControl: EditorControl<*>?
    get() = editorControlInParentChain(focusOwner)

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