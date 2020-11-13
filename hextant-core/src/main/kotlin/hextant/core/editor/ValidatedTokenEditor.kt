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
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.json.*
import reaktive.event.event
import reaktive.event.unitEvent
import reaktive.value.*
import validated.*
import validated.Validated.Valid
import validated.reaktive.ReactiveValidated

/**
 * A [ValidatedTokenEditor] is an editor whose result is always [Valid]. It can be either editable or not editable.
 * In the editable state setting the text is allowed, but the change is not immediately reflected in the [result].
 * One can commit or abort a change to get in the not editable state again and call [beginChange] to make the editor editable.
 * @param [initialText] the initial text, which has to be a valid token. Otherwise an [IllegalArgumentException] is thrown.
 */
abstract class ValidatedTokenEditor<R>(context: Context, initialText: String) :
    AbstractEditor<R, ValidatedTokenEditorView>(context), TokenType<R> {
    constructor(context: Context) : this(context, "")

    private var oldText: String = initialText
    private val _text = reactiveVariable(initialText)
    private val _editable = reactiveVariable(true)
    private val _intermediateResult = reactiveVariable(tryCompile(initialText))
    private val _result = reactiveVariable(invalidComponent<R>())

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

    final override val result: ReactiveValidated<R> get() = _result

    /**
     * The result compiled from the current [text]
     */
    val intermediateResult: ReactiveValidated<R> get() = _intermediateResult

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
    protected open fun compile(completion: Any): Validated<R> = invalidComponent()

    private fun tryCompile(item: Any): Validated<R> =
        context.executeSafely("compiling item", invalidComponent) { compile(item) }

    private fun tryCompile(text: String): Validated<R> =
        context.executeSafely("compiling item", invalidComponent) { compile(text) }

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
        if (editable.now) {
            _editable.set(false)
            _text.set(oldText)
            abortChange.fire()
            views.forEach { v ->
                v.setEditable(false)
            }
            _intermediateResult.now = tryCompile(oldText)
        }
    }

    /**
     * Commit the current change by updating the [result].
     * If the editor is not editable or the current token is not valid this method just returns.
     */
    fun commitChange(undoable: Boolean = true) {
        if (!editable.now) return
        val res = intermediateResult.now
        if (res !is Valid) return
        if (undoable) recordEdit(text.now, res.value)
        _editable.set(false)
        _result.set(res)
        oldText = text.now
        commitChange.fire(text.now)
        views.forEach { v ->
            v.setEditable(false)
        }
    }

    private fun setTextAndCommit(new: String, result: R) {
        check(!editable.now)
        _text.set(new)
        _intermediateResult.set(valid(result))
        _result.set(valid(result))
        oldText = new
        views { displayText(new) }
    }

    /**
     * Set the visible text to the [new] value and compile the new [intermediateResult].
     * @throws IllegalStateException if the editor is currently not editable.
     */
    fun setText(new: String) {
        check(editable.now) { "not editable" }
        _text.now = new
        views.forEach { v ->
            v.displayText(new)
        }
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
        views { displayText(t) }
        val res = tryCompile(completion.item).orElse { tryCompile(t) }
        _intermediateResult.now = res
        commitChange()
    }

    private fun recordEdit(t: String, res: R) {
        val oldResult = result.now
        if (oldResult is Valid) {
            val edit = CommitEdit(virtualize(), oldText, oldResult.value, t, res)
            context[UndoManager].record(edit)
        }
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
            val r = tryCompile(t).ifInvalid { return false }
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

        override fun reconstruct(original: ValidatedTokenEditor<*>) {
            original._text.now = text
            original._intermediateResult.now = original.tryCompile(original.text.now)
            original._result.now = original.intermediateResult.now
            original._editable.now = false
        }

        override fun JsonObjectBuilder.encode() {
            put("text", text)
        }

        override fun decode(element: JsonObject) {
            text = element.getValue("text").string
        }
    }

    private class CommitEdit<R>(
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