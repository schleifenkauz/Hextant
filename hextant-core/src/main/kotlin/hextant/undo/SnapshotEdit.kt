/**
 *@author Nikolaus Knop
 */

package hextant.undo

import hextant.core.Editor
import hextant.serial.Snapshot
import hextant.serial.VirtualEditor

@PublishedApi internal class SnapshotEdit<E : Editor<*>>(
    private val ref: VirtualEditor<E>,
    private val before: Snapshot<E>,
    private val after: Snapshot<E>,
    override val actionDescription: String
) : AbstractEdit() {
    override fun doRedo() {
        after.reconstructObject(ref.get())
    }

    override fun doUndo() {
        before.reconstructObject(ref.get())
    }
}