/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.PublicProperty
import bundles.publicProperty
import hextant.core.EditorView
import hextant.core.view.EditorControl
import javafx.scene.control.IndexRange
import javafx.scene.control.TextField
import reaktive.list.ReactiveList
import reaktive.list.reactiveList
import reaktive.value.ReactiveValue
import reaktive.value.ReactiveVariable
import reaktive.value.binding.map
import reaktive.value.now
import reaktive.value.reactiveVariable

/**
 * A [SelectionDistributor] keeps track of targets and views which are selected.
 */
interface SelectionDistributor {
    /**
     * All the targets which are selected
     */
    val selectedTargets: ReactiveList<Any>

    /**
     * All the views which are selected
     */
    val selectedViews: ReactiveList<EditorView>

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

    fun focus(ctrl: EditorView)

    fun saveSelectionState()
    fun restoreSelectionState()

    private class Impl : SelectionDistributor {
        override val focusedView: ReactiveVariable<EditorView?> = reactiveVariable(null)
        override val focusedTarget: ReactiveValue<Any?> = focusedView.map { it?.target }
        override val selectedViews = reactiveList<EditorView>()
        override val selectedTargets: ReactiveList<Any> = selectedViews.map { it.target }

        private var savedSelectionState: SelectionState? = null

        override fun toggleSelection(view: EditorView): Boolean {
            if (view !in selectedViews.now) {
                selectedViews.now.add(view)
                focusedView.set(view)
                return true
            } else {
                removeSelection(view)
                return false
            }
        }

        private fun removeSelection(view: EditorView) {
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

        override fun focus(ctrl: EditorView) {
            if (ctrl !in selectedViews.now) {
                select(ctrl)
            } else {
                focusedView.set(ctrl)
            }
        }

        private fun clearSelection() {
            selectedViews.now.forEach { it.deselect() }
            selectedViews.now.clear()
        }

        private fun removeAllExcept(view: EditorView) {
            selectedViews.now.forEach { v -> if (v != view) v.deselect() }
            selectedViews.now.retainAll(setOf(view))
        }

        override fun saveSelectionState() {
            val focusedView = focusedView.now
            val state = focusedView?.let(ViewState::get)
            savedSelectionState = SelectionState(selectedViews.now, focusedView, state)
        }

        override fun restoreSelectionState() {
            val (views, focused, state) = savedSelectionState ?: return
            clearSelection()
            for (view in views) {
                if (view != focused) view.toggleSelection()
            }
            if (focused != null) {
                focused.select()
                state?.restore(focused)
            }
        }

        private data class SelectionState(
            val selectedViews: List<EditorView>, val focusedView: EditorView?,
            val focusedViewState: ViewState?
        )

        private interface ViewState {
            fun restore(view: EditorView)

            data class TextFieldSelection(val range: IndexRange) : ViewState {
                override fun restore(view: EditorView) {
                    val root = (view as? EditorControl<*>)?.root as? TextField
                    if (root == null) {
                        System.err.println("Could not restore text field selection")
                        return
                    }
                    root.selectRange(range.start, range.end)
                }
            }

            companion object {
                fun get(view: EditorView): ViewState? {
                    if (view !is EditorControl<*>) return null
                    return when (val root = view.root) {
                        is TextField -> TextFieldSelection(root.selection)
                        else -> null
                    }
                }
            }
        }
    }

    companion object : PublicProperty<SelectionDistributor> by publicProperty("Selection Distributor") {
        /**
         * Return a new [SelectionDistributor]
         */
        fun newInstance(): SelectionDistributor = Impl()
    }
}