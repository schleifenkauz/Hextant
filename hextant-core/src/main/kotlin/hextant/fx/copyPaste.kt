package hextant.fx

import hextant.*
import hextant.core.Clipboard
import hextant.core.ClipboardContent.MultipleEditors
import javafx.scene.Scene

private val COPY = "Ctrl + C".shortcut
private val COPY_MANY = "Ctrl + Shift + C".shortcut
private val PASTE = "Ctrl + V".shortcut
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
    val snapshots = selected.map { (it as Editor<*>).snapshot() }
    context[Clipboard].copy(MultipleEditors(snapshots))
}

private fun Scene.copyToClipboard() {
    val view = focusedEditorControl
    val editor = view?.target
    if (editor is Editor<*>) editor.copyToClipboard()
}

internal fun Scene.registerCopyPasteShortcuts(context: Context) {
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
    }
}

