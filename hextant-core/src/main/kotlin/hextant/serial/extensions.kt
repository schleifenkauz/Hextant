/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor
import hextant.get
import reaktive.value.now

/**
 * Return the first editor in the sequence of parents which is a root editor.
 */
val Editor<*>.root: Editor<*>
    get() {
        var cur = this
        while (!cur.isRoot) {
            cur = cur.parent.now ?: error("Editor has no root")
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
        while (!cur.isRoot) {
            while (cur.expander.now != null) {
                cur = cur.expander.now!!
                accessors.add(ExpanderContent)
            }
            val acc = cur.accessor ?: error("Editor has no accessor")
            cur = cur.parent.now ?: error("Editor has no parent")
            accessors.add(acc)
        }
        accessors.reverse()
        return AccessorChain(accessors)
    }

/**
 * Virtualize this editor
 */
fun <E : Editor<*>> E.virtualize(): VirtualEditor<E> {
    val f = context[HextantFileManager].get(root)
    val loc = location
    return VirtualEditorImpl(this, f, loc)
}
