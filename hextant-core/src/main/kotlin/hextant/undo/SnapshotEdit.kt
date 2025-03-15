/**
 *@author Nikolaus Knop
 */

package hextant.undo

import hextant.core.Editor
import hextant.core.editor.Expander

@PublishedApi internal class SnapshotEdit<E : Editor<*>>(
    private val ref: Expander<*, E>,
    private val before: E,
    private val after: E,
    override val actionDescription: String
) : AbstractEdit() {
    override fun doRedo() {
        ref.expand(after)
    }

    override fun doUndo() {
        ref.expand(before)
    }
}