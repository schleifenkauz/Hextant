/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor

/**
 * The root of an editor tree
 */
interface RootEditor<R : Any> : Editor<R> {
    /**
     * Return the [HextantFile] used to save this editor
     */
    fun file(): HextantFile<out RootEditor<R>>
}