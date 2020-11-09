/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completion
import hextant.context.*
import hextant.core.Editor
import hextant.core.editor.Expander.State.Expanded
import hextant.core.editor.Expander.State.Unexpanded
import hextant.core.view.ExpanderView
import hextant.serial.*
import hextant.undo.StateTransition
import hextant.undo.UndoManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.*
import reaktive.Observer
import reaktive.value.*
import kotlin.reflect.jvm.jvmErasure

/**
 * Expanders can be imagined as placeholders for more specific editors.
 * They allow the user to type in some text and then *expand* this text into a new editor,
 * which is then substituted for the typed in text.
 */
abstract class Expander<out R, E : Editor<R>>(context: Context) : AbstractEditor<R, ExpanderView>(context) {
    private val mutex = Mutex()

    constructor(context: Context, editor: E?) : this(context) {
        if (editor != null) doChangeState(Expanded(editor))
    }

    constructor(context: Context, text: String) : this(context) {
        doChangeState(Unexpanded(text))
    }

    private sealed class State<out E> {
        class Unexpanded(val text: String) : State<Nothing>()

        class Expanded<out E>(val content: E) : State<E>()
    }

    private val undo = context[UndoManager]

    private var state: State<E> = Unexpanded("")

    private val _result = reactiveVariable(defaultResult())

    override val result: ReactiveValue<R> get() = _result

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

    private val resultType = getTypeArgument(Expander::class, 0)
    private val editorClass = getTypeArgument(Expander::class, 1).jvmErasure

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
    protected open fun defaultResult(): R =
        @Suppress("UNCHECKED_CAST")
        if (resultType.isMarkedNullable) null as R
        else error("The default implementation of defaultResult() only works for nullable result types")

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
            is Expanded -> {
                doReset()
                context.executeSafely("resetting", Unit) { onReset(oldState.content) }
                when (newState) {
                    is Unexpanded -> {
                        views { reset() }
                        doSetText(newState.text)
                    }
                    is Expanded   -> {
                        doExpandTo(newState.content)
                        context.executeSafely("expanding", Unit) { onExpansion(newState.content) }
                    }
                }
            }
            is Unexpanded -> {
                when (newState) {
                    is Unexpanded -> doSetText(newState.text)
                    is Expanded   -> {
                        doExpandTo(newState.content)
                        context.executeSafely("expanding", Unit) { onExpansion(newState.content) }
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
        editor.setAccessor(ExpanderContent)
        resultDelegator = _result.bind(editor.result)
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
        val before = snapshot()
        doChangeState(newState)
        val after = snapshot()
        if (undo.isActive) {
            val edit = StateTransition(virtualize(), before, after, actionDescription)
            undo.push(edit)
        }
    }

    /**
     * Set the text of this expander
     * @throws IllegalStateException if the expander is expanded
     */
    fun setText(newText: String) {
        check(state is Unexpanded) { "Cannot set text on expanded expander" }
        changeState(Unexpanded(newText), "Type")
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
    fun expand() {
        val state = checkUnexpanded()
        val editor = tryExpand(state.text)
        if (editor != null) changeState(Expanded(editor), "Expand")
    }

    /**
     * Use the given [completion] to expand this expander.
     * @throws IllegalStateException if the expander is already expanded
     */
    fun complete(completion: Completion<*>) {
        checkUnexpanded()
        val editor = tryExpand(completion.completion) ?: tryExpand(completion.completionText)
        if (editor != null) {
            changeState(Expanded(editor), "Complete")
        } else {
            setText(completion.completionText)
        }
    }

    /**
     * Set the wrapped editor to the specified one
     */
    fun setEditor(editor: E?) {
        if (editor == null) {
            if (state is Expanded) reset()
        } else {
            val e = editor.moveTo(contentContext())
            changeState(Expanded(e), "Change content")
        }
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
    override fun paste(snapshot: Snapshot<out Editor<*>>): Boolean {
        val editor = context.withoutUndo { snapshot.reconstructEditor(contentContext()) }
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

    override fun createSnapshot(): Snapshot<*> = Snap()

    override fun supportsCopyPaste(): Boolean = editor.now?.supportsCopyPaste() ?: true

    override fun viewAdded(view: ExpanderView) {
        when (val s = state) {
            is Unexpanded -> view.displayText(s.text)
            is Expanded -> view.expanded(s.content)
        }
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is ExpanderContent) throw InvalidAccessorException(accessor)
        return editor.now ?: throw InvalidAccessorException(accessor)
    }

    private class Snap : Snapshot<Expander<*, *>>() {
        private lateinit var state: State<Snapshot<Editor<*>>>

        override fun doRecord(original: Expander<*, *>) {
            state = when (val st = original.state) {
                is Expanded -> Expanded(st.content.snapshot(recordClass = true))
                is Unexpanded -> Unexpanded(st.text)
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(original: Expander<*, *>) {
            original as Expander<*, Editor<*>>
            when (val st = state) {
                is Expanded -> {
                    val editor = st.content.reconstructEditor(original.contentContext())
                    original.setEditor(editor)
                }
                is Unexpanded -> original.setText(st.text)
            }
        }

        override fun JsonObjectBuilder.encode() {
            when (val st = state) {
                is Expanded -> put("editor", st.content.encode())
                is Unexpanded -> put("text", st.text)
            }
        }

        override fun decode(element: JsonObject) {
            state = when {
                "editor" in element -> {
                    val editor = decode<Editor<*>>(element.getValue("editor"))
                    Expanded(editor)
                }
                "text" in element   -> {
                    val text = element.getValue("text").string
                    Unexpanded(text)
                }
                else                -> Unexpanded("")
            }
        }
    }
}