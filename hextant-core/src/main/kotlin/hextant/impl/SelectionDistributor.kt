/**
 *@author Nikolaus Knop
 */

package hextant.impl

import hextant.EditorView
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import reaktive.set.ReactiveSet
import reaktive.set.reactiveSet

interface SelectionDistributor {
    val selectedTargets: ReactiveSet<Any>

    val selectedViews: ReactiveSet<EditorView>

    fun toggleSelection(view: EditorView): Boolean

    fun select(view: EditorView): Boolean

    private class Impl: SelectionDistributor {
        override val selectedViews = reactiveSet<EditorView>()

        override val selectedTargets: ReactiveSet<Any> = selectedViews.map { it.target }

        @Synchronized override fun toggleSelection(view: EditorView): Boolean {
            if (selectedViews.now.add(view)) return true
            if (selectedTargets.now.size > 1) removeSelection(view)
            return false
        }

        private fun removeSelection(
            view: EditorView
        ) {
            if (selectedViews.now.remove(view)) {
                view.deselect()
            }
        }

        @Synchronized override fun select(view: EditorView): Boolean {
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

    companion object : Property<SelectionDistributor, Public, Public>("Selection Distributor") {
        fun newInstance(): SelectionDistributor = Impl()
    }
}