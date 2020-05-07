/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.core.editor.getSimpleEditorConstructor

/**
 * A special [Snapshot] for [Editor]s
*/
abstract class EditorSnapshot<Original : Editor<*>>(original: Original) : Snapshot<Original, Context> {
    private val originalClass = original::class

    /**
     * Reconstruct the given [editor] from this snapshot.
    */
    abstract fun reconstruct(editor: Original)

    override fun reconstruct(info: Context): Original {
        val e = originalClass.getSimpleEditorConstructor().invoke(info)
        reconstruct(e)
        return e
    }
}