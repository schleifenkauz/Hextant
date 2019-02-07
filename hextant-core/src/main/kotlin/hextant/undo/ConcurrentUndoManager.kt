/**
 *@author Nikolaus Knop
 */

package hextant.undo

class ConcurrentUndoManager(private val undo: UndoManager) : UndoManager by undo {
    @Synchronized override fun undo() {
        undo.undo()
    }

    @Synchronized override fun redo() {
        undo.redo()
    }

    @Synchronized override fun push(edit: Edit) {
        undo.push(edit)
    }
}