/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.TokenType
import hextant.core.view.FilteredTokenEditorView
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
    private var oldText = initialText
    private val _text = reactiveVariable(initialText)
    private val _editable = reactiveVariable(false)
    private val _intermediateResult = reactiveVariable(this.compile(initialText))
    private val _result = reactiveVariable(_intermediateResult.now)
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
            v.beginChange()
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
        abortChange.fire()
        views.forEach { v ->
            v.endChange()
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
        _editable.set(false)
        _result.set(res)
        oldText = text.now
        commitChange.fire(text.now)
        views.forEach { v ->
            v.endChange()
        }
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

    override fun copyForImpl(context: Context): Editor<R> {
        abortChange()
        return constructor.newInstance(context, text.now)
    }

    override fun viewAdded(view: FilteredTokenEditorView) {
        if (editable.now) view.beginChange()
        view.displayText(_text.now)
    }

    override fun serialize(output: Output, context: SerialContext) {
        abortChange()
        output.writeString(text.now)
    }

    override fun deserialize(input: Input, context: SerialContext) {
        _text.now = input.readString()
        _intermediateResult.now = compile(text.now)
        _result.now = intermediateResult.now
    }
}