/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.TokenType
import hextant.core.view.FilteredTokenEditorView
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kserial.*
import reaktive.event.event
import reaktive.event.unitEvent
import reaktive.value.*

/**
 * A [FilteredTokenEditor] is an editor whose result is always [Ok]. It can be either editable or not editable.
 * In the editable state setting the text is allowed, but the change is not immediately reflected in the [result].
 * One can commit or abort a change to get in the not editable state again and call [beginChange] to make the editor editable.
 * @param [initialText] the initial text, which has to be a valid token. Otherwise an [IllegalArgumentException] is thrown.
 */
abstract class FilteredTokenEditor<R : Any>(context: Context, initialText: String = "") :
    AbstractEditor<R, FilteredTokenEditorView>(context), TokenType<R>, Serializable {
    private var oldText: String = initialText
    private val _text = reactiveVariable(initialText)
    private val _editable = reactiveVariable(true)
    private val _intermediateResult = reactiveVariable(this.compile(initialText))
    private val _result = reactiveVariable(childErr<R>())

    private val beginChange = unitEvent()
    private val abortChange = unitEvent()
    private val commitChange = event<String>()

    private val constructor = javaClass.getConstructor(Context::class.java, String::class.java)

    /**
     * The visible text
     */
    val text: ReactiveString get() = _text

    /**
     * This [ReactiveBoolean] is only `true` if the editor is currently editable and allows setting the text.
     */
    val editable: ReactiveBoolean get() = _editable

    final override val result: EditorResult<R> get() = _result

    /**
     * The result compiled from the current [text]
     */
    val intermediateResult: EditorResult<R> get() = _intermediateResult

    /**
     * Emits events when the editor becomes editable
     */
    val beganChange get() = beginChange.stream

    /**
     * Emits an event if a change is aborted
     */
    val abortedChange get() = abortChange.stream

    /**
     * Emits an event if a change is commited
     */
    val commitedChange get() = commitChange.stream

    /**
     * Begin a change. If the editor is already editable this method just returns.
     */
    fun beginChange() {
        if (editable.now) return
        _editable.set(true)
        beginChange.fire()
        views.forEach { v ->
            v.setEditable(true)
        }
    }

    /**
     * Abort the current change by setting the text back to the value before the change was began.
     * If the editor is not editable this method just returns.
     */
    fun abortChange() {
        if (!editable.now) return
        _editable.set(false)
        _text.set(oldText)
        _intermediateResult.now = compile(oldText)
        abortChange.fire()
        views.forEach { v ->
            v.setEditable(false)
        }
    }

    /**
     * Commit the current change by updating the [result].
     * If the editor is not editable or the current token is not valid this method just returns.
     */
    fun commitChange() {
        if (!editable.now) return
        val res = intermediateResult.now
        if (res !is Ok) return
        val edit = CommitEdit(oldText, text.now)
        _editable.set(false)
        _result.set(res)
        oldText = text.now
        commitChange.fire(text.now)
        views.forEach { v ->
            v.setEditable(false)
        }
        context[UndoManager].push(edit)
    }

    private fun setTextAndCommit(new: String) {
        check(!editable.now)
        val res = compile(new)
        if (res !is Ok) error("Illegal token $new")
        _text.set(new)
        _intermediateResult.set(res)
        _result.set(res)
        oldText = new
        views { displayText(new) }
    }

    /**
     * Set the visible text to the [new] value. And compile the new [intermediateResult].
     * @throws IllegalStateException if the editor is currently not editable.
     */
    fun setText(new: String) {
        check(editable.now)
        _text.now = new
        _intermediateResult.now = compile(new)
        views.forEach { v ->
            v.displayText(new)
        }
    }

    fun recompile() {
        _intermediateResult.set(compile(text.now))
    }

    override fun paste(editor: Editor<*>): Boolean {
        if (editor !is FilteredTokenEditor) return false
        if (editable.now) setText(editor.text.now)
        else setTextAndCommit(editor.text.now)
        return true
    }

    override fun viewAdded(view: FilteredTokenEditorView) {
        view.setEditable(editable.now)
        view.displayText(_text.now)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(text.now)
    }

    override fun deserialize(input: Input, context: SerialContext) {
        _text.now = input.readString()
        _intermediateResult.now = compile(text.now)
        _result.now = intermediateResult.now
        _editable.now = false
    }

    private inner class CommitEdit(private val old: String, private val new: String) : AbstractEdit() {
        override fun doUndo() {
            setTextAndCommit(old)
        }

        override fun doRedo() {
            setTextAndCommit(new)
        }

        override val actionDescription: String
            get() = "Commit edit"
    }
}