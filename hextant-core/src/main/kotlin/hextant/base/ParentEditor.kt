/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*

abstract class ParentEditor<out E : Editable<*>, V : EditorView>(editable: E, context: Context) :
    AbstractEditor<E, V>(editable, context) {
    /**
     * @return `true` only if the specified [child] can be child of this parent
     */
    protected abstract fun accepts(child: Editor<*>): Boolean

    @Deprecated("Only called by AbstractEditor")
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

    private var lastExtendingChild: Editor<*>? = null

    override fun extendSelection(child: Editor<*>) {
        if (!child.isSelected) throw IllegalStateException("Cannot extend selection from unselected child")
        if (isSelected) return
        toggleSelection()
        child.toggleSelection()
        lastExtendingChild = child
    }

    override fun shrinkSelection() {
        val childToSelect = lastExtendingChild ?: children.firstOrNull()
        if (childToSelect != null && !childToSelect.isSelected && childToSelect in allChildren) {
            childToSelect.toggleSelection()
            if (isSelected) toggleSelection()
        }
    }
}