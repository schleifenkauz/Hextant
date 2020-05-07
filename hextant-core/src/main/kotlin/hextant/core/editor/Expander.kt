/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import hextant.core.editor.Expander.State.Expanded
import hextant.core.editor.Expander.State.Unexpanded
import hextant.core.view.ExpanderView
import hextant.serial.*
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kserial.*
import reaktive.Observer
import reaktive.value.*
import reaktive.value.binding.map
import kotlin.reflect.full.isSubclassOf

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
    }

    private val undo = context[UndoManager]

    private var state: State<E> = Unexpanded("")

    private val _result = reactiveVariable<CompileResult<R>>(childErr())

    override val result: EditorResult<R> get() = _result

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
     * @return the editor that should be wrapped if the expander is expanded with the given [text] or `null` if the text
     * is not valid
     */
    protected abstract fun expand(text: String): E?

    /**
     * Can be overwritten by extending classes to be notified when [expand] was successfully called
     */
    protected open fun onExpansion(editor: E) {}

    /**
     * Can be overwritten by extending classes to be notified when [reset] was successfully called
     */
    protected open fun onReset(editor: E) {}

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

    private fun doChangeState(newState: State<E>, notify: Boolean = true) {
        val oldState = state
        state = newState
        when (oldState) {
            is Expanded   -> {
                unexpand()
                if (notify) onReset(oldState.editor)
                when (newState) {
                    is Unexpanded -> {
                        views { reset() }
                        doSetText(newState.text)
                    }
                    is Expanded   -> {
                        doExpandTo(newState.editor)
                        if (notify) onExpansion(newState.editor)
                    }
                }
            }
            is Unexpanded -> {
                when (newState) {
                    is Unexpanded -> doSetText(newState.text)
                    is Expanded   -> {
                        doExpandTo(newState.editor)
                        if (notify) onExpansion(newState.editor)
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

    private fun unexpand() {
        resultDelegator?.kill()
        resultDelegator = null
        _editor.set(null)
        _result.set(childErr())
    }

    @Suppress("DEPRECATION")
    private fun doExpandTo(editor: E) {
        this.parent?.let { editor.initParent(it) }
        editor.initExpander(this)
        editor.initAccessor(ExpanderContent)
        resultDelegator = _result.bind(editor.result.map { it.orElse { childErr() } })
        _editor.set(editor)
        _text.set(null)
        views { expanded(editor) }
    }

    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
    override fun initParent(parent: Editor<*>) {
        super.initParent(parent)
        editor.now?.initParent(parent)
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
        val e = editor.moveTo(contentContext())
        changeState(Expanded(e), "Change content")
    }

    /**
     * Reset the expander by setting the text to the empty string
     * @throws IllegalStateException if not expanded
     */
    fun reset() {
        check(state is Expanded) { "Cannot reset unexpanded expander" }
        changeState(Unexpanded(""), "Reset")
    }

    @Suppress("UNCHECKED_CAST")
    override fun paste(editor: Editor<*>): Boolean = when {
        editor is Expander<*, *> && editor.editorClass.isSubclassOf(this.editorClass) -> {
            val text = editor.text.now
            val e = editor.editor.now
            when {
                text != null -> {
                    setText(text)
                    true
                }
                e != null    -> pasteContent(e)
                else         -> true
            }
        }
        editorClass.isInstance(editor) && accepts(editor as E)                        -> pasteContent(editor)
        else                                                                          -> false
    }

    @Suppress("UNCHECKED_CAST")
    private fun pasteContent(e: Editor<*>): Boolean {
        val new = e::class.getSimpleEditorConstructor().invoke(contentContext())
        val supported = new.paste(e)
        if (supported) changeState(Expanded(new as E), "Paste")
        return supported
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeObject(text.now)
        editor.now?.let { e ->
            output.writeString(e::class.java.name)
            output.writeUntyped(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(input: Input, context: SerialContext) {
        val text = input.readTyped<String?>()
        if (text != null) doChangeState(Unexpanded(text), notify = false)
        else {
            val name = input.readString()
            val cls = Class.forName(name).kotlin
            val editor = cls.getSimpleEditorConstructor().invoke(contentContext())
            doChangeState(Expanded(editor as E), notify = false)
            input.readInplace(editor)
        }
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is ExpanderContent) throw InvalidAccessorException(accessor)
        return editor.now ?: throw InvalidAccessorException(accessor)
    }

    private class Snapshot(original: Expander<*, *>) : EditorSnapshot<Expander<*, *>>(original) {
        private val text = original.text.now
        private val content = original.editor.now?.createSnapshot()

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: Expander<*, *>) {
            editor as Expander<*, Editor<*>>
            if (text != null) editor.setText(text)
            else editor.setEditor(content!!.reconstruct(editor.context))
        }
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