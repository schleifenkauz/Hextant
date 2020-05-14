/**
 *@author Nikolaus Knop
 */

package hextant.undo

import hextant.Editor
import hextant.base.EditorSnapshot
import hextant.serial.VirtualEditor

/**
 * An edit that is characterized by two states ([EditorSnapshot]s) of which one represents
 * the state before the edit and one represents the state after the edit.
 */
class SnapshotEdit<E : Editor<*>>(
    private val editor: VirtualEditor<E>,
    private val old: EditorSnapshot<E>,
    private val new: EditorSnapshot<E>,
    override val actionDescription: String
) : AbstractEdit() {
    override fun doRedo() {
        new.reconstruct(editor.get())
    }

    override fun doUndo() {
        old.reconstruct(editor.get())
    }
}