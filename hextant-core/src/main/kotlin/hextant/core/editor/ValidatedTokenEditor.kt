/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completion
import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.core.view.ValidatedTokenEditorView
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.event.event
import reaktive.event.unitEvent
import reaktive.value.*

/**
 * A [ValidatedTokenEditor] is an editor whose result is always non-null. It can be either editable or not editable.
 * In the editable state setting the text is allowed, but the change is not immediately reflected in the [result].
 * One can commit or abort a change to get in the not editable state again and call [beginChange] to make the editor editable.
 */
@Serializable
abstract class ValidatedTokenEditor<R : Any>() : AbstractEditor<R, ValidatedTokenEditorView>(), TokenType<R?> {
    private lateinit var _text: ReactiveVariable<String>

    @Transient
    private lateinit var oldText: String

    @Transient
    private val _editable: ReactiveVariable<Boolean> = reactiveVariable(false)

    @Transient
    private lateinit var _intermediateResult: ReactiveVariable<R?>

    @Transient
    private lateinit var _result: ReactiveVariable<R>

    fun setInitialText(initialText: String) {
        oldText = initialText
        _text = reactiveVariable(initialText)
        _editable.set(true)
    }

    @Transient
    private val beginChange = unitEvent()

    @Transient
    private val abortChange = unitEvent()

    @Transient
    private val commitChange = event<String>()

    override fun initialize(context: Context) {
        super.initialize(context)
        _intermediateResult = reactiveVariable(tryCompile(text.now))
        _result = reactiveVariable(tryCompile(text.now) ?: defaultResult())
    }

    /**
     * The visible text
     */
    val text: ReactiveString get() = _text

    /**
     * This [ReactiveBoolean] is only `true` if the editor is currently editable and allows setting the text.
     */
    val editable: ReactiveBoolean get() = _editable

    final override val result: ReactiveValue<R> get() = _result

    /**
     * The result compiled from the current [text]
     */
    val intermediateResult: ReactiveValue<R?> get() = _intermediateResult

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
     * Compile a result from the given completion.
     */
    protected open fun compile(completion: Any): R? = null

    /**
     * Return the result that this editor should have initially, if doesn't recognize the initial text.
     *
     * The default implementation throws an [IllegalStateException]
     */
    protected open fun defaultResult(): R = error("defaultResult() was not overwritten")

    private fun tryCompile(item: Any): R? =
        context.executeSafely("compiling item", null) { compile(item) }

    private fun tryCompile(text: String): R? =
        context.executeSafely("compiling item", null) { compile(text) }

    /**
     * Begin a change. If the editor is already editable this method just returns.
     */
    fun beginChange() {
        if (editable.now) return
        _editable.set(true)
        beginChange.fire()
        notifyViews { setEditable(true) }
    }

    /**
     * Abort the current change by setting the text back to the value before the change was began.
     * If the editor is not editable this method just returns.
     */
    fun abortChange() {
        if (editable.now) {
            _editable.set(false)
            _text.set(oldText)
            abortChange.fire()
            notifyViews { setEditable(false) }
            _intermediateResult.now = tryCompile(oldText)
        }
    }

    /**
     * Commit the current change by updating the [result].
     * If the editor is not editable or the current token is not valid this method just returns.
     */
    fun commitChange(undoable: Boolean = true) {
        if (!editable.now) return
        val res = intermediateResult.now ?: return
        if (undoable) recordEdit(text.now, res)
        _editable.set(false)
        _result.set(res)
        oldText = text.now
        commitChange.fire(text.now)
        notifyViews { setEditable(false) }
    }

    private fun setTextAndCommit(new: String, result: R) {
        check(!editable.now)
        _text.set(new)
        _intermediateResult.set(result)
        _result.set(result)
        oldText = new
        notifyViews { displayText(new) }
    }

    /**
     * Set the visible text to the [new] value and compile the new [intermediateResult].
     * @throws IllegalStateException if the editor is currently not editable.
     */
    fun setText(new: String) {
        check(editable.now) { "not editable" }
        _text.now = new
        notifyViews { displayText(new) }
        _intermediateResult.now = tryCompile(new)
    }

    /**
     * Set the visible text to the [Completion.completionText] of the given [completion] and commit the result.
     * @throws IllegalStateException if the editor is currently not editable.
     */
    fun complete(completion: Completion<*>) {
        check(editable.now) { "not editable" }
        val t = completion.completionText
        _text.now = t
        notifyViews { displayText(t) }
        val res = tryCompile(completion.item) ?: tryCompile(t)
        _intermediateResult.now = res
        commitChange()
    }

    private fun recordEdit(t: String, res: R) {
        val oldResult = result.now
        val edit = CommitEdit(this, oldText, oldResult, t, res)
        context[UndoManager].record(edit)
    }

    /**
     * Recompile the [intermediateResult].
     */
    fun recompile() {
        _intermediateResult.set(tryCompile(text.now))
    }

    override fun paste(editor: Editor<*>): Boolean {
        if (editor !is ValidatedTokenEditor) return false
        val t = editor.text.now
        if (editable.now) setText(t)
        else {
            val r = tryCompile(t) ?: return false
            setTextAndCommit(t, r)
        }
        return true
    }

    override fun viewAdded(view: ValidatedTokenEditorView) {
        view.setEditable(editable.now)
        view.displayText(_text.now)
    }

    private class CommitEdit<R : Any>(
        private val editor: ValidatedTokenEditor<R>,
        private val old: String, private val oldResult: R,
        private val new: String, private val newResult: R
    ) : AbstractEdit() {
        override fun doUndo() {
            editor.setTextAndCommit(old, oldResult)
        }

        override fun doRedo() {
            editor.setTextAndCommit(new, newResult)
        }

        override val actionDescription: String
            get() = "Commit edit"
    }
}