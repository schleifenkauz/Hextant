/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.impl.SelectionDistributor
import hextant.inspect.Inspections
import reaktive.value.*
import reaktive.value.base.AbstractVariable

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
    context: Context
) : Editor<E>, AbstractController<V>() {

    private val inspections = context[Inspections]

    private val hasWarning = inspections.hasWarning(editable)

    private val hasError = inspections.hasError(editable)

    private val warningObserver = hasWarning.observe { warn: Boolean ->
        views { warn(warn) }
    }

    private val errorObserver = hasError.observe { isError: Boolean ->
        views { error(isError) }
    }

    override fun onGuiThread(view: V, action: V.() -> Unit) {
        view.onGuiThread { view.action() }
    }

    override fun viewAdded(view: V) {
        with(view) {
            onGuiThread {
                warn(hasWarning.now)
                error(hasError.now)
                select(isSelected)
            }
        }
    }

    private val selectionDistributor = context[SelectionDistributor]

    final override val isSelected: Boolean get() = isSelectedVar.get()

    private val isSelectedVar: Variable<Boolean> = object : AbstractVariable<Boolean>() {
        private var isSelected = false

        override fun doSet(value: Boolean) {
            if (!value) {
                lastExtendingChild = null
            }
            isSelected = value
            views.forEach { it.select(isSelected = value) }
        }

        override fun get(): Boolean = isSelected
    }

    override fun select() {
        selectionDistributor.select(this, isSelectedVar)
    }

    final override fun toggleSelection() {
        selectionDistributor.toggleSelection(this, isSelectedVar)
    }

    private var lastExtendingChild: Editor<*>? = null

    override fun extendSelection(child: Editor<*>) {
        throw IllegalStateException("Cannot extend selection for a non-parent editor")
    }

    /**
     * Shrink the selection by selecting the last child that extended selection to this editor
     * or the first child if there is no such child
     */
    override fun shrinkSelection() {}

    override val allChildren: Sequence<Editor<*>>
        get() = children.asSequence().flatMap { c -> c.allChildren + c }

    override fun moveTo(newParent: ParentEditor<*, *>?) {
        if (newParent == null) {
            parent = null
            return
        }
        if (parent == newParent) return
        @Suppress("DEPRECATION") //only called here
        newParent.accept(this)
        parent = newParent
    }

    final override var parent: Editor<*>? = null
        private set
}