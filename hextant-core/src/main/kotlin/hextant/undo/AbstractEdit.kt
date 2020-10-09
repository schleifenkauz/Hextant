/**
 *@author Nikolaus Knop
 */

package hextant.undo

/**
 * Skeletal implementation of [Edit]
 */
abstract class AbstractEdit : Edit {
    private var undone = false

    /**
     * Actually redo the [Edit], state is already checked
     */
    protected abstract fun doRedo()

    /**
     * Actually undo the [Edit], state is already checked
     */
    protected abstract fun doUndo()

    final override val canRedo: Boolean
        get() = undone

    final override val canUndo: Boolean
        get() = !undone

    final override fun redo() {
        check(canRedo) { "Edit already redone" }
        doRedo()
        undone = false
    }

    final override fun undo() {
        check(canUndo) { "Edit already undone" }
        doUndo()
        undone = true
    }

    /**
     * Merges this [Edit] with the [other] Edit if possible or returns `null`
     * * Undoing the resulting [Edit] should behave like undoing the other edit and then this one
     * * Redoing the resulting [Edit] should behave like redoing this edit and then the other one
     * * Default implementation, returns `null`
     */
    override fun mergeWith(other: Edit): Edit? = null
}