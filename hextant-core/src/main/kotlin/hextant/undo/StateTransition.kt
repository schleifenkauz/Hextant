/**
 *@author Nikolaus Knop
 */

package hextant.undo

import hextant.core.Editor
import hextant.serial.Snapshot
import hextant.serial.VirtualEditor

internal class StateTransition<E : Editor<*>>(
    private val ref: VirtualEditor<E>,
    private val before: Snapshot<E>,
    private val after: Snapshot<E>,
    override val actionDescription: String
) : AbstractEdit() {
    override fun doRedo() {
        after.reconstruct(ref.get())
    }

    override fun doUndo() {
        before.reconstruct(ref.get())
    }
}