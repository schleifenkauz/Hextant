/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.*
import hextant.base.EditorControl
import hextant.bundle.CoreProperties
import hextant.core.view.FXExpanderView
import hextant.impl.SelectionDistributor
import hextant.impl.Stylesheets
import javafx.scene.*
import javafx.scene.control.Label
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent

internal var isShiftDown = false; private set

fun hextantScene(
    root: (Context) -> Parent,
    createContext: (HextantPlatform) -> Context
): Scene {
    val platform = HextantPlatform.configured()
    val context = createContext(platform)
    val scene = Scene(root(context))
    scene.initHextantScene(context)
    return scene
}

fun Scene.initHextantScene(context: Context) {
    initEventHandlers(context)
    context[Stylesheets].apply(this)
}

private fun Scene.initEventHandlers(ctx: Context) {
    listenForShift()
    changeTraversalEngine()
    traverseOnArrowWithCtrl()
    registerCopyPaste(ctx)
}

private fun Scene.registerCopyPaste(context: Context) {
    registerShortcuts {
        on("Ctrl + C") {
            val view = focusedEditorControl
            val editor = view?.target
            if (editor is Editor<*>) editor.copyToClipboard()
        }
        on("Ctrl + Shift + C") {
            val selected = context[SelectionDistributor].selectedTargets.now
            if (selected.any { it !is Editor<*> }) return@on
            context[CoreProperties.clipboard] = selected.toList()
        }
        on("Ctrl + V") {
            val view = focusedEditorControl
            val editor = view?.target
            if (editor is Editor<*>) {
                val success = editor.pasteFromClipboard()
                if (!success) editor.expander?.pasteFromClipboard()
            }
        }
    }
}

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

internal fun Scene.traverseOnArrowWithCtrl() {
    registerShortcuts {
        on("Ctrl + Left") {
            focusPrevious()
        }
        //TODO(think about this)
        //        on("Ctrl + Shift + Left") {
        //            deselectFocusedAndFocusPrevious()
        //        }
        on("Ctrl + Shift? + Right") {
            focusNext()
        }
    }
}

fun Scene.deselectFocusedAndFocusPrevious() {
    val focused = focusedEditorControl ?: return
    focused.toggleSelection()
    val prev = focused.previousEditorControl
    prev?.justFocus()
}

internal fun Scene.focusNext(): Boolean {
    val next = focusedEditorControl?.next ?: return false
    val firstChild = generateSequence(next) {
        if (it is FXExpanderView) it.root as? EditorControl<*>
        else it.editorChildren().firstOrNull()
    }.last()
    firstChild.focus()
    return true
}

private val Node.previousEditorControl
    get() = generateSequence(editorControlInParentChain(this)?.previous) {
        if (it is FXExpanderView) it.root as? EditorControl<*>
        else it.editorChildren().lastOrNull()
    }.lastOrNull()

internal fun Scene.focusPrevious(): Boolean {
    val prev = focusOwner.previousEditorControl
    prev?.focus()
    return prev != null
}

private val Scene.focusedEditorControl: EditorControl<*>?
    get() = editorControlInParentChain(focusOwner)

private fun editorControlInParentChain(node: Node) =
    generateSequence(node) { it.parent }.firstOrNull { it is EditorControl<*> } as EditorControl<*>?

private val TRAV_NEXT = "TAB".shortcut
private val TRAV_PREV = "Shift + TAB".shortcut

@Suppress("DEPRECATION")
private fun Scene.changeTraversalEngine() {
    registerShortcuts(KeyEvent.ANY) {
        on(TRAV_NEXT) {
            if (it.eventType == KeyEvent.KEY_RELEASED) focusedEditorControl?.next?.select()
        }
        on(TRAV_PREV) {
            if (it.eventType == KeyEvent.KEY_RELEASED) focusedEditorControl?.previous?.select()
        }
    }
}

fun lastShortcutLabel(scene: Scene): Label {
    val shortcutDisplay = Label().apply {
        style = "-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 20;"
    }
    scene.addEventFilter(KeyEvent.KEY_RELEASED) { e ->
        if (e.isShortcut() || e.code == ENTER || e.code == TAB) {
            shortcutDisplay.text = e.getShortcutString()
        }
    }
    return shortcutDisplay
}

private fun KeyEvent.getShortcutString(): String = buildString {
    if (isControlDown) append("Ctrl + ")
    if (isAltDown) append("Alt + ")
    if (isShiftDown) append("Shift + ")
    if (isMetaDown) append("Meta + ")
    append(code)
}

private fun KeyEvent.isShortcut() = isAltDown || isControlDown || isShortcutDown || isMetaDown