/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.editor.Expander.State.Expanded
import hextant.core.editor.Expander.State.Unexpanded
import hextant.core.view.ExpanderView
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import reaktive.Observer
import reaktive.value.*
import reaktive.value.binding.map

abstract class Expander<out R : Any, E : Editor<R>>(context: Context) : AbstractEditor<R, ExpanderView>(context) {
    private sealed class State<out R, out E> {
        class Unexpanded(val text: String) : State<Nothing, Nothing>()

        class Expanded<out R : Any, E : Editor<R>>(val editor: E) : State<R, E>()
    }

    private val undo = context[UndoManager]

    private var state: State<R, E> = Unexpanded("")

    private val _result = reactiveVariable<CompileResult<R>>(childErr())

    override val result: EditorResult<R> get() = _result

    private var resultDelegator: Observer? = null

    private var parentBinder: Observer? = null

    val isExpanded: Boolean get() = state is Expanded

    private val _editor = reactiveVariable<E?>(null)

    val editor: ReactiveValue<E?> get() = _editor

    protected abstract fun expand(text: String): E?

    private fun doChangeState(newState: State<R, E>) {
        val oldState = state
        state = newState
        when (oldState) {
            is Expanded   -> {
                unexpand()
                _result.set(childErr())
                when (newState) {
                    is Unexpanded -> views { reset() }
                    is Expanded   -> doExpandTo(newState.editor)
                }
            }
            is Unexpanded -> {
                when (newState) {
                    is Unexpanded -> views { displayText(newState.text) }
                    is Expanded   -> doExpandTo(newState.editor)
                }
            }
        }
    }

    private fun unexpand() {
        resultDelegator?.kill()
        parentBinder?.kill()
        resultDelegator = null
        parentBinder = null
        _editor.set(null)
    }

    private fun doExpandTo(editor: E) {
        resultDelegator = _result.bind(editor.result.map { it.orElse { childErr() } })
        parentBinder = this.parent.forEach { editor.setParent(it) }
        _editor.set(editor)
        editor.setParent(this.parent.now)
        editor.setExpander(this)
        views { expanded(editor) }
    }

    private fun changeState(newState: State<R, E>, actionDescription: String) {
        val edit = StateTransition(state, newState, actionDescription)
        doChangeState(newState)
        undo.push(edit)
    }

    fun setText(newText: String) {
        check(state is Unexpanded) { "Cannot set text on expanded expander" }
        changeState(Unexpanded(newText), "Type")
    }

    fun expand() {
        val state = state
        check(state is Unexpanded) { "Cannot expand expanded expander" }
        val editor = expand(state.text) ?: return
        changeState(Expanded(editor), "Expand")
    }

    fun setEditor(editor: E) {
        changeState(Expanded(editor), "Change content")
    }

    fun reset() {
        check(state is Expanded) { "Cannot reset unexpanded expander" }
        changeState(Unexpanded(""), "Reset")
    }

    override fun viewAdded(view: ExpanderView) {
        when (val state = state) {
            is Unexpanded -> view.displayText(state.text)
            is Expanded   -> view.expanded(state.editor)
        }
    }

    private inner class StateTransition(
        private val old: State<R, E>,
        private val new: State<R, E>,
        override val actionDescription: String
    ) : AbstractEdit() {
        override fun doRedo() {
            doChangeState(new)
        }

        override fun doUndo() {
            doChangeState(old)
        }
    }
}