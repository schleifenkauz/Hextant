package hextant.serial

import hextant.core.Editor

class EditorReference<E : Editor<*>> internal constructor(
    private val root: Editor<*>, private val location: EditorLocation<E>
) {
    fun get(): E = location.locateIn(root)
}

