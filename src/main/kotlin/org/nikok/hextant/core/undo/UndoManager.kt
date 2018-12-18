/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

/**
 * The UndoManager is responsible for managing stacks of edits and undo and redo them
 */
interface UndoManager {
    /**
     * @return `true` only if this [UndoManager] can currently undo an [Edit]
     */
    val canUndo: Boolean

    /**
     * @return `true` only if this [UndoManager] can currently redo an [Edit]
     */
    val canRedo: Boolean

    /**
     * Undo the last done or redone [Edit]
     */
    fun undo()

    /**
     * Redo the last undone [Edit]
     */
    fun redo()

    /**
     * The human-readable description of the currently undoable [Edit]
     */
    val undoText: String

    /**
     * The human-readable description of the currently undoable [Edit]
     */
    val redoText: String
}