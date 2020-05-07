/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Editor

/**
 * This editor snapshot reconstructs editors component-wise.
 */
internal class CompoundEditorSnapshot<Original : CompoundEditor<*>>(original: Original) :
    EditorSnapshot<Original>(original) {
    private val snapshots = original.children.now.map { it.createSnapshot() }

    @Suppress("UNCHECKED_CAST")
    override fun reconstruct(editor: Original) {
        for ((e, s) in editor.children.now.zip(snapshots)) {
            s as EditorSnapshot<Editor<*>>
            s.reconstruct(e)
        }
    }
}