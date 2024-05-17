/**
 * @author Nikolaus Knop
 */

package hextant.undo

import hextant.context.Context
import hextant.core.Editor
import hextant.fx.KeyEventHandlerBody
import hextant.fx.registerShortcuts
import hextant.serial.snapshot
import hextant.serial.virtualize
import javafx.scene.Node

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

/**
 * Execute the given [action] and record an edit with the specified [description] that will reset
 * this editor to the state before [action] was executed if undone.
 */
inline fun <E : Editor<*>, R> E.makeUndoableEdit(description: String, action: () -> R): R {
    val undo = context[UndoManager]
    if (!undo.isActive) return action()
    val before = snapshot()
    val result = action()
    val after = snapshot()
    val edit = SnapshotEdit(virtualize(), before, after, description)
    undo.record(edit)
    return result
}

fun Node.registerHistoryShortcuts(manager: UndoManager) {
    registerShortcuts {
        historyShortcuts(manager)
    }
}

fun KeyEventHandlerBody<Unit>.historyShortcuts(manager: UndoManager) {
    on("Ctrl?+Z") {
        manager.undo()
    }
    on("Ctrl?+Shift+Z") {
        manager.redo()
    }
}