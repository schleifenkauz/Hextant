package hextant.fx

import fxutils.registerShortcuts
import fxutils.shortcut
import hextant.context.Clipboard
import hextant.context.ClipboardContent.MultipleEditors
import hextant.context.Context
import hextant.context.SelectionDistributor
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.core.editor.copyToClipboard
import hextant.core.editor.pasteFromClipboard
import hextant.core.editor.snapshot
import javafx.scene.Scene

private val COPY_MANY = "Ctrl + Shift + C".shortcut

private fun Scene.copyManyToClipboard(context: Context) {
    val control = focusedEditorControl ?: return
    val selected = control.context[SelectionDistributor].selectedTargets.now
    if (selected.any { it !is Editor<*> }) return
    val snapshots = selected.map {
        context.executeSafely("copying", null) {
            (it as Editor<*>).snapshot()
        } ?: return
    }
    context[Clipboard].copy(MultipleEditors(snapshots))
}

fun Scene.registerCopyPasteShortcuts(context: Context) {
    registerShortcuts {
        on("Ctrl+C") { ev ->
            val control = focusedEditorControl ?: return@on
            val selected = control.context[SelectionDistributor].selectedTargets.now.singleOrNull() ?: return@on
            if (selected !is Editor<*>) return@on
            if (selected.copyToClipboard()) ev.consume()
        }
        on("Ctrl+V") { ev ->
            val control = editorControlInParentChain(focusOwner) ?: return@on
            val selected = control.context[SelectionDistributor].selectedTargets.now.singleOrNull() ?: return@on
            if (selected !is Editor<*>) return@on
            if (selected.pasteFromClipboard()) ev.consume()
        }
        on(COPY_MANY) {
            copyManyToClipboard(context)
        }
    }
}

