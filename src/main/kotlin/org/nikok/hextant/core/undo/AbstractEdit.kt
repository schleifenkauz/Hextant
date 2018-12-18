/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.undo

abstract class AbstractEdit : Edit {
    /**
     * Actually redo the [Edit], state is already checked
     */
    protected abstract fun doRedo()

    /**
     * Actually undo the [Edit], state is already checked
     */
    protected abstract fun doUndo()

    private var undone = false

    override val canRedo: Boolean
        get() = undone

    override val canUndo: Boolean
        get() = !undone

    override fun redo() {
        check(canRedo) { "Edit already redone" }
        doRedo()
        undone = false
    }

    override fun undo() {
        check(canUndo) { "Edit already undone" }
        doUndo()
        undone = true
    }
}