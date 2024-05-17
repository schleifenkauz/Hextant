/**
 *@author Nikolaus Knop
 */

package hextant.undo

import reaktive.value.*
import java.util.*

internal class UndoManagerImpl : UndoManager {
    private val redoable = LinkedList<Edit>()
    private val undoable = LinkedList<Edit>()
    private var compound: MutableList<Edit>? = null

    private val _canUndo = reactiveVariable(false)
    private val _canRedo = reactiveVariable(false)
    private val _undoText = reactiveVariable("Cannot undo")
    private val _redoText = reactiveVariable("Cannot redo")

    override val canUndo: ReactiveBoolean get() = _canUndo
    override val canRedo: ReactiveBoolean get() = _canRedo
    override val undoText: ReactiveString
        get() = _undoText
    override val redoText: ReactiveString
        get() = _redoText

    override var isActive: Boolean = true

    private fun updateReactiveVariables() {
        _canRedo.now = redoable.isNotEmpty()
        _redoText.now = redoable.peek()?.run { "Redo $actionDescription" } ?: "Cannot redo"
        _canUndo.now = undoable.isNotEmpty()
        _undoText.now = undoable.peek()?.run { "Undo $actionDescription" } ?: "Cannot undo"
    }

    override fun undo() {
        check(canUndo.now) { "Cannot undo" }
        check(compound == null) { "Undo during compound edit is not possible" }
        val e = undoable.poll()
        println("Undo ${e.actionDescription}")
        withoutUndo { e.undo() }
        redoable.push(e)
        updateReactiveVariables()
    }

    override fun redo() {
        check(canRedo.now) { "Cannot redo" }
        check(compound == null) { "Redo during compound edit is not possible" }
        val e = redoable.poll()
        println("Redo ${e.actionDescription}")
        withoutUndo { e.redo() }
        undoable.push(e)
        updateReactiveVariables()
    }

    override fun record(edit: Edit) {
        if (!isActive) return
        check(edit.canUndo) { "Attempt to push non-undoable edit to UndoManager" }
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
        updateReactiveVariables()
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
        record(e)
    }
}