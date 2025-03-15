/**
 * @author Nikolaus Knop
 */

package hextant.undo

import fxutils.KeyEventHandlerBody
import fxutils.registerShortcuts
import hextant.context.Context
import javafx.scene.Node
import reaktive.value.now

/**
 * Calls [UndoManager.beginCompoundEdit] before [UndoManager.finishCompoundEdit] after executing the given [actions].
 * @param description the [Edit.actionDescription] of the resulting compound edit.
 */
inline fun <T> Context.compoundEdit(description: String, actions: () -> T): T = with(get(UndoManager)) {
    beginCompoundEdit()
    val res = try {
        actions()
    } finally {
        finishCompoundEdit(description)
    }
    res
}

/**
 * Deactivates the [UndoManager] while executing the given [action] and then reactivates it.
 * @see UndoManager.isActive
 * @see UndoManager.record
 */
inline fun <T> UndoManager.withoutUndo(action: () -> T): T {
    if (!isActive) return action()
    isActive = false
    try {
        return action()
    } catch (ex: Throwable) {
        throw ex
    } finally {
        isActive = true
    }
}

fun Node.registerHistoryShortcuts(manager: UndoManager) {
    registerShortcuts {
        historyShortcuts(manager)
    }
}

fun KeyEventHandlerBody<Unit>.historyShortcuts(manager: UndoManager) {
    on("Ctrl+Z") {
        if (manager.canUndo.now) manager.undo()
    }
    on("Ctrl+Shift+Z") {
        if (manager.canRedo.now) manager.redo()
    }
}