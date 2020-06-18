/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import hextant.completion.Completion
import hextant.context.*
import hextant.core.Editor
import hextant.core.editor.Expander.State.Expanded
import hextant.core.editor.Expander.State.Unexpanded
import hextant.core.moveTo
import hextant.core.view.ExpanderView
import hextant.serial.*
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import reaktive.Observer
import reaktive.value.*
import reaktive.value.binding.map
import validated.*
import validated.reaktive.ReactiveValidated

/**
 * An Expander acts like a wrapper around editors.
 */
abstract class Expander<out R, E : Editor<R>>(context: Context) : AbstractEditor<R, ExpanderView>(context) {
    constructor(context: Context, editor: E?) : this(context) {
        if (editor != null) doChangeState(Expanded(editor))
    }

    constructor(context: Context, text: String) : this(context) {
        doChangeState(Unexpanded(text))
    }

    private sealed class State<out E> {
        class Unexpanded(val text: String) : State<Nothing>()

        class Expanded<out E>(val editor: E) : State<E>()
    }

    private val undo = context[UndoManager]

    private var state: State<E> = Unexpanded("")

    private val _result = reactiveVariable(defaultResult())

    override val result: ReactiveValidated<R> get() = _result

    private var resultDelegator: Observer? = null

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

    private val constructor by lazy { this::class.getSimpleEditorConstructor() }

    @Suppress("UNCHECKED_CAST")
    private val editorClass by lazy { getTypeArgument(Expander::class, 1) }

    /**
     * Return the editor that should be wrapped if the expander
     * is expanded with the given [text] or `null` if the text is not valid.
     */
    protected abstract fun expand(text: String): E?

    /**
     * Create an editor from the given [completion] or return `null` if it is not possible.
     *
     * The default implementation returns `null`.
     */
    protected open fun expand(completion: Any): E? = null

    /**
     * Can be overwritten by extending classes to be notified when [expand] was successfully called
     */
    protected open fun onExpansion(editor: E) {}

    /**
     * Can be overwritten by extending classes to be notified when [reset] was successfully called
     */
    protected open fun onReset(editor: E) {}

    /**
     * Returns the result that this expander should have if it is not expanded.
     */
    protected open fun defaultResult(): Validated<R> = invalidComponent()

    /**
     * Return `true` iff the given editor can be the content of this expander.
     * The defaults implementation always returns `true`
     */
    protected open fun accepts(editor: E): Boolean = true

    /**
     * Returns the [Context] that expanded editors should be using.
     * The default implementation returns the `context` of this Expander.
     */
    protected open fun contentContext(): Context = context

    private fun doChangeState(newState: State<E>) {
        val oldState = state
        state = newState
        when (oldState) {
            is Expanded   -> {
                doReset()
                context.executeSafely("resetting", Unit) { onReset(oldState.editor) }
                when (newState) {
                    is Unexpanded -> {
                        views { reset() }
                        doSetText(newState.text)
                    }
                    is Expanded   -> {
                        doExpandTo(newState.editor)
                        context.executeSafely("expanding", Unit) { onExpansion(newState.editor) }
                    }
                }
            }
            is Unexpanded -> {
                when (newState) {
                    is Unexpanded -> doSetText(newState.text)
                    is Expanded   -> {
                        doExpandTo(newState.editor)
                        context.executeSafely("expanding", Unit) { onExpansion(newState.editor) }
                    }
                }
            }
        }
    }

    private fun doSetText(text: String) {
        _text.set(text)
        views { displayText(text) }
    }

    private fun resetViews() {
        views { reset() }
    }

    private fun doReset() {
        resultDelegator?.kill()
        resultDelegator = null
        _editor.set(null)
        _result.set(defaultResult())
    }

    @Suppress("DEPRECATION")
    private fun doExpandTo(editor: E) {
        this.parent?.let { editor.initParent(it) }
        editor.initExpander(this)
        editor.initAccessor(ExpanderContent)
        resultDelegator = _result.bind(editor.result.map { it.or(invalidComponent()) })
        _editor.set(editor)
        _text.set(null)
        views { expanded(editor) }
    }

    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
    override fun initParent(parent: Editor<*>) {
        super.initParent(parent)
        editor.now?.initParent(parent)
    }

    private fun changeState(newState: State<E>, actionDescription: String, undoable: Boolean) {
        if (undoable) {
            val edit = StateTransition(
                expander = virtualize(),
                old = state.createSnapshot(),
                new = newState.createSnapshot(),
                actionDescription = actionDescription
            )
            undo.push(edit)
        }
        doChangeState(newState)
    }

    /**
     * Set the text of this expander
     * @throws IllegalStateException if the expander is expanded
     */
    fun setText(newText: String, undoable: Boolean = true) {
        check(state is Unexpanded) { "Cannot set text on expanded expander" }
        changeState(Unexpanded(newText), "Type", undoable)
    }

    private fun checkUnexpanded(): Unexpanded {
        val state = state
        check(state is Unexpanded) { "Cannot expand expanded expander" }
        return state
    }

    private fun tryExpand(text: String) = context.executeSafely("expanding", null) { expand(text) }
    private fun tryExpand(item: Any) = context.executeSafely("expanding", null) { expand(item) }

    /**
     * Expand the current text
     * @throws IllegalStateException if already expanded
     */
    fun expand(undoable: Boolean = true) {
        val state = checkUnexpanded()
        val editor = tryExpand(state.text) ?: return
        changeState(Expanded(editor), "Expand", undoable)
    }

    /**
     * Use the given [completion] to expand this expander.
     * @throws IllegalStateException if the expander is already expanded
     */
    fun complete(completion: Completion<*>) {
        checkUnexpanded()
        val editor = tryExpand(completion.completion) ?: tryExpand(completion.completionText)
        if (editor != null) {
            changeState(Expanded(editor), "Complete", undoable = true)
        } else {
            setText(completion.completionText)
        }
    }

    /**
     * Set the wrapped editor to the specified one
     */
    fun setEditor(editor: E, undoable: Boolean = true) {
        val e = editor.moveTo(contentContext())
        changeState(Expanded(e), "Change content", undoable)
    }

    /**
     * Reset the expander by setting the text to the empty string
     * @throws IllegalStateException if not expanded
     */
    fun reset(undoable: Boolean = true) {
        check(state is Expanded) { "Cannot reset unexpanded expander" }
        changeState(Unexpanded(""), "Reset", undoable)
    }

    @Suppress("UNCHECKED_CAST")
    override fun paste(snapshot: EditorSnapshot<*>): Boolean {
        val editor = snapshot.reconstruct(contentContext())
        return when {
            editor is Expander<*, *>                               -> {
                val text = editor.text.now
                val e = editor.editor.now
                when {
                    text != null                                              -> {
                        setText(text)
                        true
                    }
                    e != null && editorClass.isInstance(e) && accepts(e as E) -> {
                        setEditor(e)
                        true
                    }
                    else                                                      -> false
                }
            }
            editorClass.isInstance(editor) && accepts(editor as E) -> {
                setEditor(editor)
                true
            }
            else                                                   -> false
        }
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is ExpanderContent) throw InvalidAccessorException(accessor)
        return editor.now ?: throw InvalidAccessorException(accessor)
    }

    private class Snapshot(original: Expander<*, *>) : EditorSnapshot<Expander<*, *>>(original) {
        private val state = original.state.createSnapshot()

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: Expander<*, *>) {
            editor as Expander<*, Editor<*>>
            editor.doChangeState(state.reconstruct(editor.contentContext()))
        }
    }

    private class StateTransition<E : Editor<*>>(
        private val expander: VirtualEditor<Expander<*, E>>,
        private val old: State<EditorSnapshot<E>>,
        private val new: State<EditorSnapshot<E>>,
        override val actionDescription: String
    ) : AbstractEdit() {
        override fun doRedo() {
            val e = expander.get()
            e.doChangeState(new.reconstruct(e.contentContext()))
        }

        override fun doUndo() {
            val e = expander.get()
            e.doChangeState(old.reconstruct(e.contentContext()))
        }
    }

    companion object {
        private fun <E : Editor<*>> State<E>.createSnapshot(): State<EditorSnapshot<E>> = when (this) {
            is Unexpanded -> this
            is Expanded   -> Expanded(editor.snapshot())
        }

        @Suppress("UNCHECKED_CAST")
        private fun <E : Editor<*>> State<EditorSnapshot<*>>.reconstruct(context: Context): State<E> = when (this) {
            is Unexpanded -> this
            is Expanded   -> Expanded(editor.reconstruct(context) as E)
        }
    }
}