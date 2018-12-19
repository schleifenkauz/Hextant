/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

import java.util.*

class UndoManagerImpl : UndoManager {
    private val redoable = LinkedList<Edit>()

    private val undoable = LinkedList<Edit>()

    override val canUndo: Boolean
        get() = undoable.isNotEmpty()

    override val canRedo: Boolean
        get() = redoable.isNotEmpty()

    override fun undo() {
        check(canUndo) { "Cannot undo" }
        val e = undoable.poll()
        e.undo()
        redoable.push(e)
    }

    override fun redo() {
        check(canRedo) { "Cannot redo" }
        val e = redoable.poll()
        e.redo()
        undoable.push(e)
    }

    override fun push(edit: Edit) {
        check(edit.canUndo) { "Attempt to Non-undoable edit push to UndoManager" }
        redoable.clear()
        val last = undoable.peekFirst()
        val e = if (last != null) {
            last.mergeWith(edit) ?: edit
        } else edit
        redoable.push(e)
    }

    override val undoText: String
        get() = redoable.peek()?.run { "Redo $actionDescription" } ?: "Cannot redo"
    override val redoText: String
        get() = redoable.peek()?.run { "Redo $actionDescription" } ?: "Cannot redo"
}