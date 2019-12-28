/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor

internal class AccessorChain<E : Editor<*>>(private val accessors: List<EditorAccessor>) : EditorLocation<E> {
    @Suppress("UNCHECKED_CAST")
    override fun locateIn(root: Editor<*>): E {
        var cur = root
        for (p in accessors) {
            cur = cur.getSubEditor(p)
        }
        return cur as E
    }
}