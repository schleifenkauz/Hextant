/**
 * @author Nikolaus Knop
 */

package hextant.undo

import hextant.context.Context

/**
 * Calls [UndoManager.beginCompoundEdit] before [UndoManager.finishCompoundEdit] after executing the given [actions].
 * @param description the [Edit.actionDescription] of the resulting compound edit.
 */
inline fun <T> Context.compoundEdit(description: String, actions: () -> T): T = with(get(UndoManager)) {
    beginCompoundEdit()
    val res = actions()
    finishCompoundEdit(description)
    res
}

/**
 * Deactivates the [UndoManager] while executing the given [action] and then reactivates it.
 * @see UndoManager.isActive
 * @see UndoManager.push
 */
inline fun <T> UndoManager.withoutUndo(action: () -> T): T {
    if (!isActive) return action()
    isActive = false
    try {
        return action()
    } finally {
        isActive = true
    }
}