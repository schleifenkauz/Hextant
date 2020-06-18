/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.core.Editor

/**
 * Return the first editor in the sequence of parents which is a root editor.
 */
val Editor<*>.root: Editor<*>
    get() {
        var cur = this
        while (!cur.isRoot) {
            cur = cur.parent ?: error("Editor has no root")
        }
        return cur
    }

/**
 * @return the location of this editor relative to its root
 */
val <E : Editor<*>> E.location: EditorLocation<E>
    get() {
        var cur: Editor<*> = this
        val accessors = mutableListOf<EditorAccessor>()
        while (true) {
            if (cur.isRoot) break
            while (cur.expander != null) {
                cur = cur.expander!!
                if (cur.isRoot) break
                accessors.add(ExpanderContent)
            }
            if (cur.isRoot) break
            val acc = cur.accessor ?: error("Editor has no accessor")
            cur = cur.parent ?: error("Editor has no parent")
            accessors.add(acc)
        }
        accessors.reverse()
        return AccessorChain(accessors)
    }

/**
 * Virtualize this editor
 */
fun <E : Editor<*>> E.virtualize(): VirtualEditor<E> = LocatedVirtualEditor(this, file!!, location)

/**
 * Makes this editor a root of the editor tree by assigning an [InMemoryFile]
 */
fun Editor<*>.makeRoot() {
    @Suppress("DEPRECATION")
    setFile(InMemoryFile(this))
}
