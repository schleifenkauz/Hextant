/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.base.CompoundEditor
import hextant.base.EditorSnapshot
import hextant.snapshot

/**
 * This editor snapshot reconstructs editors component-wise.
 */
internal class CompoundEditorSnapshot<Original : CompoundEditor<*>>(original: Original) :
    EditorSnapshot<Original>(original) {
    private val snapshots = original.children.now.map { it.snapshot() }

    @Suppress("UNCHECKED_CAST")
    override fun reconstruct(editor: Original) {
        for ((e, s) in editor.children.now.zip(snapshots)) {
            s.reconstruct(e)
        }
    }
}