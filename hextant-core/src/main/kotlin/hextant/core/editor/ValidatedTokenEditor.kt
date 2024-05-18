/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completion
import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.core.view.ValidatedTokenEditorView
import hextant.serial.*
import hextant.serial.string
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put
import reaktive.event.event
import reaktive.event.unitEvent
import reaktive.value.*

/**
 * A [ValidatedTokenEditor] is an editor whose result is always non-null. It can be either editable or not editable.
 * In the editable state setting the text is allowed, but the change is not immediately reflected in the [result].
 * One can commit or abort a change to get in the not editable state again and call [beginChange] to make the editor editable.
 * @param [initialText] the initial text, if this is not a valid token the [defaultResult] function is used.
 */
abstract class ValidatedTokenEditor<R : Any>(context: Context, initialText: String) :
    AbstractEditor<R, ValidatedTokenEditorView>(context), TokenType<R?> {
    constructor(context: Context) : this(context, "")

    private var oldText: String = initialText
    private val _text = reactiveVariable(initialText)
    private val _editable = reactiveVariable(true)
    private val _intermediateResult = reactiveVariable(tryCompile(initialText))

    @Suppress("LeakingThis")
    private val _result = reactiveVariable(compile(initialText) ?: defaultResult())

    private val beginChange = unitEvent()
    private val abortChange = unitEvent()
    private val commitChange = event<String>()

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
        val edit = CommitEdit(virtualize(), oldText, oldResult, t, res)
        context[UndoManager].record(edit)
    }

    /**
     * Recompile the [intermediateResult].
     */
    fun recompile() {
        _intermediateResult.set(tryCompile(text.now))
    }

    override fun paste(snapshot: Snapshot<out Editor<*>>): Boolean {
        if (snapshot !is Snap) return false
        val t = snapshot.text
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

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Snap : Snapshot<ValidatedTokenEditor<*>>() {
        lateinit var text: String

        override fun doRecord(original: ValidatedTokenEditor<*>) {
            text = original.text.now
        }

        override fun reconstructObject(original: ValidatedTokenEditor<*>) {
            original._text.now = text
            original._intermediateResult.now = original.tryCompile(original.text.now)
            original._result.now = original.intermediateResult.now!!
            original._editable.now = false
        }

        override fun encode(builder: JsonObjectBuilder) {
            builder.put("text", this.text)
        }

        override fun decode(element: JsonObject) {
            text = element.getValue("text").string
        }
    }

    private class CommitEdit<R : Any>(
        private val editor: VirtualEditor<ValidatedTokenEditor<R>>,
        private val old: String, private val oldResult: R,
        private val new: String, private val newResult: R
    ) : AbstractEdit() {
        override fun doUndo() {
            editor.get().setTextAndCommit(old, oldResult)
        }

        override fun doRedo() {
            editor.get().setTextAndCommit(new, newResult)
        }

        override val actionDescription: String
            get() = "Commit edit"
    }
}