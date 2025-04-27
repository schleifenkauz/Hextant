/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.PublicProperty
import bundles.publicProperty
import hextant.core.EditorView
import hextant.core.editor.allChildren
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
    fun toggleSelection(view: EditorView)

    /**
     * Select the given [view].
     * Causes the view to be the only selected view of this distributor.
     */
    fun select(view: EditorView)

    fun focus(view: EditorView)

    fun saveSelectionState()
    fun restoreSelectionState()

    private class Impl : SelectionDistributor {
        override val focusedView: ReactiveVariable<EditorView?> = reactiveVariable(null)
        override val focusedTarget: ReactiveValue<Any?> = focusedView.map { it?.target }
        override val selectedViews = reactiveList<EditorView>()
        override val selectedTargets: ReactiveList<Any> = selectedViews.map { it.target }

        private var savedSelectionState: SelectionState? = null

        override fun toggleSelection(view: EditorView) {
            if (view !in selectedViews.now) {
                selectedViews.now.add(view)
                view.displaySelected(true)
                setFocus(view)
                for (v in selectedViews.now.toList()) {
                    if (v.target.allChildren.contains(view.target)) {
                        deselect(v)
                    }
                }
            } else {
                deselect(view)
            }
        }

        private fun deselect(v: EditorView) {
            selectedViews.now.remove(v)
            v.displaySelected(false)
        }

        override fun select(view: EditorView) {
            setFocus(view)
            val views = selectedViews.now
            val alreadySelected = view in views
            if (views.size == 1 && alreadySelected) return
            else if (alreadySelected) {
                removeAllExcept(view)
            } else {
                view.displaySelected(true)
                clearSelection()
                selectedViews.now.add(view)
            }
        }

        override fun focus(view: EditorView) {
            if (view !in selectedViews.now) {
                select(view)
            } else {
                setFocus(view)
            }
        }

        private fun setFocus(view: EditorView) {
            focusedView.set(view)
            view.focus()
        }

        private fun clearSelection() {
            selectedViews.now.forEach { it.displaySelected(false) }
            selectedViews.now.clear()
        }

        private fun removeAllExcept(view: EditorView) {
            selectedViews.now.forEach { v -> if (v != view) v.displaySelected(false) }
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