/**
 *@author Nikolaus Knop
 */

package hextant.undo

internal class CompoundEdit(private val edits: List<Edit>, override val actionDescription: String) : AbstractEdit() {
    override fun doRedo() {
        for (e in edits) e.redo()
    }

    override fun doUndo() {
        for (e in edits.asReversed()) e.undo()
    }
}