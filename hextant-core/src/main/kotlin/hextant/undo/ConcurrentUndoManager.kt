/**
 *@author Nikolaus Knop
 */

package hextant.undo

/**
 * An [UndoManager] the delegates to the given [wrapped] manager but synchronizes calls.
 */
class ConcurrentUndoManager(private val wrapped: UndoManager) : UndoManager by wrapped {
    @Synchronized override fun undo() {
        wrapped.undo()
    }

    @Synchronized override fun redo() {
        wrapped.redo()
    }

    @Synchronized override fun record(edit: Edit) {
        wrapped.record(edit)
    }
}