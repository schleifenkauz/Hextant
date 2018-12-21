/**
 *@author Nikolaus Knop
 */

package hextant.undo

/**
 * Skeletal implementation of [Edit]
 */
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
     * Default implementation, returns `null`
     */
    override fun mergeWith(other: Edit): Edit? = null
}