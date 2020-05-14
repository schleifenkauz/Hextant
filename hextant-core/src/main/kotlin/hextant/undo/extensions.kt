/**
 * @author Nikolaus Knop
 */

package hextant.undo

import hextant.Context

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