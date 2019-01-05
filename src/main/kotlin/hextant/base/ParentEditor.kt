/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*

abstract class ParentEditor<E : Editable<*>, V : EditorView>(editable: E, context: Context) :
    AbstractEditor<E, V>(editable, context) {
    /**
     * @return `true` only if the specified [child] can be child of this parent
     */
    protected abstract fun accepts(child: Editor<*>): Boolean

    @Suppress("UNCHECKED_CAST")
    internal open fun accept(child: Editor<*>) {
        if (!accepts(child)) {
            throw IllegalArgumentException("$this does not accept $child as a child")
        }
        mutableChildren.add(child)
    }

    private val mutableChildren: MutableCollection<Editor<*>> = mutableSetOf()

    /**
     * @return the direct children of this [Editable]
     */
    override val children: Collection<Editor<*>> get() = mutableChildren

}