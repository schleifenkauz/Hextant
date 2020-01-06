/**
 * @author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor
import reaktive.value.now

/**
 * Return the first editor in the sequence of parents which is a [RootEditor]. Note that [RootEditor]s may have parents
 */
val Editor<*>.root: RootEditor<*>
    get() {
        var cur = this
        while (cur !is RootEditor) {
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
        while (cur !is RootEditor) {
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
    val r = root
    val f = r.file()
    @Suppress("UNCHECKED_CAST")
    val loc = location
    return VirtualEditorImpl(this, f, loc)
}
