/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.impl.SelectionDistributor
import org.nikok.reaktive.value.Variable
import org.nikok.reaktive.value.base.AbstractVariable
import org.nikok.reaktive.value.observe

/**
 * The base class of all [Editor]s
 * It manages selection and showing errors of the [Editable]s in the associated [EditorView]
 * @constructor
 * @param E the type of [Editable] edited by this [Editor]
 * @param V the type of [EditorView]'s that can be managed by this editor
 * @param editable the [Editable] edited by this [Editor]
 */
abstract class AbstractEditor<out E : Editable<*>, V : EditorView>(
    final override val editable: E,
    private val context: Context
) : Editor<E>, AbstractController<V>() {

    private val isOkObserver = editable.isOk.observe("Observe isOk") { isOk ->
        views.forEach { v -> v.error(isError = !isOk) }
    }

    override fun onGuiThread(view: V, action: V.() -> Unit) {
        view.onGuiThread { view.action() }
    }

    private val selectionDistributor = context[SelectionDistributor]

    final override val isSelected: Boolean get() = isSelectedVar.get()

    private val isSelectedVar: Variable<Boolean> = object : AbstractVariable<Boolean>() {
        private var isSelected = false

        override val description: String
            get() = "Is ${this@AbstractEditor} selected"

        override fun doSet(value: Boolean) {
            if (!value) {
                lastExtendingChild = null
                deselectChildren()
            }
            isSelected = value
            views.forEach { it.select(isSelected = value) }
        }

        override fun get(): Boolean = isSelected
    }

    private fun deselectChildren() {
        allChildren?.forEach { c ->
            if (c.isSelected) c.toggleSelection()
        }
    }

    override fun select() {
        selectionDistributor.select(this, isSelectedVar)
    }

    final override fun toggleSelection() {
        selectionDistributor.toggleSelection(this, isSelectedVar)
    }

    private var lastExtendingChild: Editor<*>? = null

    override fun extendSelection(child: Editor<*>) {
        if (editable is ParentEditable<*, *>) {
            require(child.isSelected) { "Child is not selected" }
            toggleSelection()
            child.toggleSelection()
            lastExtendingChild = child
        } else throw IllegalStateException("Cannot extend selection for a non-parent editor")
    }

    override fun shrinkSelection() {
        require(isSelected) { "Shrinking parent is not selected" }
        if (editable is ParentEditable<*, *>) {
            val c = lastExtendingChild ?: children!!.first()
            if (!c.isSelected) c.toggleSelection()
            toggleSelection()
        }
    }

    override val parent: Editor<*>?
        get() = editable.parent?.let { p -> context.getEditor(p) }

    override val children: Collection<Editor<*>>?
        get() = (editable as? ParentEditable<*, *>)?.children?.map { child -> context.getEditor(child) }

    override val allChildren: Sequence<Editor<*>>?
        get() =
            if (editable is ParentEditable<*, *>) editable.allChildren.map(context::getEditor)
            else null
}