/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.core.Editor

/**
 * Used to resolve [Editor]s from the root
 */
interface EditorLocation<out E : Editor<*>> {
    /**
     * Locate the editor from the given [root]
     */
    fun locateIn(root: Editor<*>): E
}