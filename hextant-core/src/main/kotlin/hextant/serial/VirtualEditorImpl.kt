/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor
import java.lang.ref.WeakReference

internal class VirtualEditorImpl<E : Editor<*>>(
    ref: E,
    private val root: HextantFile<out RootEditor<*>>,
    private val location: EditorLocation<E>
) : VirtualEditor<E> {
    private var weak = WeakReference(ref)

    override fun get(): E {
        weak.get()?.let { return it }
        val r = root.get()
        val e = location.locateIn(r)
        weak = WeakReference(e)
        return e
    }
}