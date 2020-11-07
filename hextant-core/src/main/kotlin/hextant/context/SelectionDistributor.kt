/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.PublicProperty
import bundles.publicProperty
import hextant.core.EditorView
import reaktive.set.ReactiveSet
import reaktive.set.reactiveSet
import reaktive.value.*
import reaktive.value.binding.map

/**
 * A [SelectionDistributor] keeps track of targets and views which are selected.
 */
interface SelectionDistributor {
    /**
     * All the targets which are selected
     */
    val selectedTargets: ReactiveSet<Any>

    /**
     * All the views which are selected
     */
    val selectedViews: ReactiveSet<EditorView>

    /**
     * The target that was selected most recently
     */
    val focusedTarget: ReactiveValue<Any?>

    /**
     * The view that was selected most recently
     */
    val focusedView: ReactiveValue<EditorView?>

    /**
     * Toggles the selection of the given [view] and its target.
     * If the view was selected before and there are other selected views than it is deselected.
     * If it is not selected currently it is selected in addition to the other selected views.
     */
    fun toggleSelection(view: EditorView): Boolean

    /**
     * Select the given [view].
     * Causes the view to be the only selected view of this distributor.
     */
    fun select(view: EditorView): Boolean

    private class Impl : SelectionDistributor {
        override val focusedView: ReactiveVariable<EditorView?> = reactiveVariable(null)
        override val focusedTarget: ReactiveValue<Any?> = focusedView.map { it?.target }
        override val selectedViews = reactiveSet<EditorView>()
        override val selectedTargets: ReactiveSet<Any> = selectedViews.map { it.target }

        override fun toggleSelection(view: EditorView): Boolean {
            if (selectedViews.now.add(view)) {
                focusedView.set(view)
                return true
            }
            if (selectedTargets.now.size > 1) {
                removeSelection(view)
                return false
            }
            return true
        }

        private fun removeSelection(
            view: EditorView
        ) {
            if (selectedViews.now.remove(view)) {
                view.deselect()
            }
        }

        override fun select(view: EditorView): Boolean {
            focusedView.set(view)
            val views = selectedViews.now
            val alreadySelected = view in views
            if (views.size == 1 && alreadySelected) return true
            else if (alreadySelected) {
                removeAllExcept(view)
            } else {
                clearSelection()
                selectedViews.now.add(view)
            }
            return true
        }

        private fun clearSelection() {
            selectedViews.now.forEach { it.deselect() }
            selectedViews.now.clear()
        }

        private fun removeAllExcept(view: EditorView) {
            selectedViews.now.forEach { v -> if (v != view) v.deselect() }
            selectedViews.now.retainAll(setOf(view))
        }
    }

    companion object : PublicProperty<SelectionDistributor> by publicProperty("Selection Distributor") {
        /**
         * Return a new [SelectionDistributor]
         */
        fun newInstance(): SelectionDistributor =
            Impl()
    }
}