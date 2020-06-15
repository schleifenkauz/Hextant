/**
 *@author Nikolaus Knop
 */

package hextant.undo

import java.util.*

internal class UndoManagerImpl : UndoManager {
    private val redoable = LinkedList<Edit>()

    private val undoable = LinkedList<Edit>()

    private var compound: MutableList<Edit>? = null

    override val canUndo: Boolean
        get() = undoable.isNotEmpty()

    override val canRedo: Boolean
        get() = redoable.isNotEmpty()

    override fun undo() {
        check(canUndo) { "Cannot undo" }
        check(compound == null) { "Undo during compound edit is not possible" }
        val e = undoable.poll()
        println("Undo ${e.actionDescription}")
        e.undo()
        redoable.push(e)
    }

    override fun redo() {
        check(canRedo) { "Cannot redo" }
        check(compound == null) { "Redo during compound edit is not possible" }
        val e = redoable.poll()
        println("Redo ${e.actionDescription}")
        e.redo()
        undoable.push(e)
    }

    override fun push(edit: Edit) {
        check(edit.canUndo) { "Attempt to Non-undoable edit push to UndoManager" }
        if (compound != null) {
            compound!!.add(edit)
            return
        }
        redoable.clear()
        val last = undoable.peekFirst()
        if (last != null) {
            val merged = last.mergeWith(edit)
            if (merged != null) {
                undoable.poll()
                undoable.push(merged)
            } else {
                undoable.push(edit)
            }
        } else undoable.push(edit)
    }

    override fun beginCompoundEdit() {
        check(compound == null) { "Compound edit already began" }
        compound = mutableListOf()
    }

    override fun finishCompoundEdit(description: String) {
        val edits = compound
        check(edits != null) { "Compound edit was not started" }
        compound = null
        if (edits.isEmpty()) return
        val e = CompoundEdit(edits, description)
        push(e)
    }

    override val undoText: String
        get() = undoable.peek()?.run { "Undo $actionDescription" } ?: "Cannot undo"
    override val redoText: String
        get() = redoable.peek()?.run { "Redo $actionDescription" } ?: "Cannot redo"
}