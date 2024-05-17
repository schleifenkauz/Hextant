package hextant.undo

import reaktive.value.ReactiveBoolean
import reaktive.value.ReactiveString
import reaktive.value.reactiveValue

/**
 * An [UndoManager] that never records edits
 */
object NoUndoManager : UndoManager {
    override val canUndo: ReactiveBoolean
        get() = reactiveValue(false)
    override val canRedo: ReactiveBoolean
        get() = reactiveValue(false)

    override var isActive: Boolean = true

    override fun undo() {}

    override fun redo() {}

    override fun record(edit: Edit) {}

    override val undoText: ReactiveString
        get() = reactiveValue("No undo")
    override val redoText: ReactiveString
        get() = reactiveValue("No redo")

    override fun beginCompoundEdit() {}

    override fun finishCompoundEdit(description: String) {}
}