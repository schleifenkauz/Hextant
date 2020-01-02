/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties.clipboard
import hextant.core.editor.Expander.State.Expanded
import hextant.core.editor.Expander.State.Unexpanded
import hextant.core.view.ExpanderView
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kserial.*
import reaktive.Observer
import reaktive.value.*
import reaktive.value.binding.map

/**
 * An Expander acts like a wrapper around editors.
 */
abstract class Expander<out R : Any, E : Editor<R>>(context: Context) : AbstractEditor<R, ExpanderView>(context),
                                                                        Serializable {
    constructor(context: Context, editor: E?) : this(context) {
        if (editor != null) doChangeState(Expanded(editor))
    }

    constructor(context: Context, text: String) : this(context) {
        doChangeState(Unexpanded(text))
    }

    private sealed class State<out E : Editor<*>> {
        class Unexpanded(val text: String) : State<Nothing>()

        class Expanded<E : Editor<*>>(val editor: E) : State<E>()

        @Suppress("UNCHECKED_CAST")
        fun copyState() = if (this is Expanded) Expanded(editor.copyForImpl(editor.context) as E) else this
    }

    private val undo = context[UndoManager]

    private var state: State<E> = Unexpanded("")

    private val _result = reactiveVariable<CompileResult<R>>(childErr())

    override val result: EditorResult<R> get() = _result

    private var resultDelegator: Observer? = null

    private var parentBinder: Observer? = null

    /**
     * @return `true` only if the expander is expanded
     */
    val isExpanded: Boolean get() = state is Expanded

    private val _editor = reactiveVariable<E?>(null)

    /**
     * A [ReactiveValue] holding the currently wrapped editor or `null` if the expander is not expanded
     */
    val editor: ReactiveValue<E?> get() = _editor

    private val _text = reactiveVariable<String?>(null)

    /**
     * A [ReactiveValue] holding the current text of the editor or `null` if it is expanded
     */
    val text: ReactiveValue<String?> get() = _text

    private val constructor by lazy { javaClass.getConstructor(Context::class.java) }

    /**
     * @return the editor that should be wrapped if the expander is expanded with the given [text] or `null` if the text
     * is not valid
     */
    protected abstract fun expand(text: String): E?

    /**
     * Return `true` iff the given editor can be the content of this expander.
     * Default implementation simply returns `false`
     */
    protected open fun accepts(editor: Editor<*>): Boolean = false

    private fun doChangeState(newState: State<E>) {
        val oldState = state
        state = newState
        when (oldState) {
            is Expanded   -> {
                unexpand()
                _result.set(childErr())
                when (newState) {
                    is Unexpanded -> {
                        resetViews()
                        onSetText(newState.text)
                    }
                    is Expanded   -> doExpandTo(newState.editor)
                }
            }
            is Unexpanded -> {
                when (newState) {
                    is Unexpanded -> onSetText(newState.text)
                    is Expanded   -> doExpandTo(newState.editor)
                }
            }
        }
    }

    private fun onSetText(text: String) {
        _text.set(text)
        views { displayText(text) }
    }

    private fun resetViews() {
        views { reset() }
    }

    private fun unexpand() {
        resultDelegator?.kill()
        parentBinder?.kill()
        resultDelegator = null
        parentBinder = null
        _editor.set(null)
    }

    @Suppress("DEPRECATION")
    private fun doExpandTo(editor: E) {
        resultDelegator = _result.bind(editor.result.map { it.orElse { childErr() } })
        parentBinder = this.parent.forEach { editor.setParent(it) }
        _editor.set(editor)
        _text.set(null)
        editor.setParent(this.parent.now)
        editor.setExpander(this)
        views { expanded(editor) }
    }

    private fun changeState(newState: State<E>, actionDescription: String) {
        val edit = StateTransition(state, newState, actionDescription)
        doChangeState(newState)
        undo.push(edit)
    }

    /**
     * Set the text of this expander
     * @throws IllegalStateException if the expander is expanded
     */
    fun setText(newText: String) {
        check(state is Unexpanded) { "Cannot set text on expanded expander" }
        changeState(Unexpanded(newText), "Type")
    }

    /**
     * Expand the current text
     * @throws IllegalStateException if already expanded
     */
    fun expand() {
        val state = state
        check(state is Unexpanded) { "Cannot expand expanded expander" }
        val editor = expand(state.text) ?: return
        changeState(Expanded(editor), "Expand")
    }

    /**
     * Set the wrapped editor to the specified one
     */
    fun setEditor(editor: E) {
        changeState(Expanded(editor), "Change content")
    }

    /**
     * Reset the expander by setting the text to the empty string
     * @throws IllegalStateException if not expanded
     */
    fun reset() {
        check(state is Expanded) { "Cannot reset unexpanded expander" }
        changeState(Unexpanded(""), "Reset")
    }

    override fun copyForImpl(context: Context): Editor<R> {
        val copy = constructor.newInstance(context) as Expander<R, E>
        copy.doChangeState(this.state.copyState())
        return copy
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeObject(text.now)
        output.writeObject(editor.now)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(input: Input, context: SerialContext) {
        val text = input.readTyped<String?>()
        if (text != null) setText(text)
        val editor = input.readObject() as E?
        if (editor != null) setEditor(editor)
    }

    /**
     * Put the current editor into the clipboard.
     * If the expander is not expanded or the current editor doesn't support copying this method has no effect.
     */
    fun copy() {
        val e = editor.now ?: return
        if (!e.supportsCopy()) return
        context[Internal, clipboard] = e
    }

    /**
     * Set the editor of this expander to a copy of the clipboard editor this expander can accept it
     */
    fun paste() {
        val content = context[Public, clipboard] as? Editor<*> ?: return
        check(content.supportsCopy()) { "Content in clipboard does not support copying" }
        if (!accepts(content)) return
        val copy = content.copyForImpl(this.context)
        check(copy.javaClass == content.javaClass) { "Copy returned object of different class" }
        @Suppress("UNCHECKED_CAST")
        setEditor(copy as E)
    }

    private inner class StateTransition(
        private val old: State<E>,
        private val new: State<E>,
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