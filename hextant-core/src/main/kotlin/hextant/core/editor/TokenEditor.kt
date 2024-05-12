/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completion
import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.view.TokenEditorView
import hextant.serial.Snapshot
import hextant.serial.VirtualEditor
import hextant.serial.string
import hextant.serial.virtualize
import hextant.undo.AbstractEdit
import hextant.undo.Edit
import hextant.undo.UndoManager
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import reaktive.value.ReactiveString
import reaktive.value.ReactiveValue
import reaktive.value.now
import reaktive.value.reactiveVariable
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure

/**
 * A token editor transforms text to tokens.
 * When setting the text it is automatically compiled to a token.
 */
abstract class TokenEditor<out R, in V : TokenEditorView>(context: Context, text: String) :
    AbstractEditor<R, V>(context), TokenType<R> {
    private val resultType = this::class.memberFunctions.first { it.name == "defaultResult" }.returnType

    constructor(context: Context) : this(context, "")

    private val _result by lazy { reactiveVariable(tryCompile(text)) }

    override val result: ReactiveValue<R> get() = _result

    private var _text = reactiveVariable(text)

    /**
     * A [ReactiveValue] holding the current textual content of this editor
     */
    val text: ReactiveString get() = _text

    private val undo = context[UndoManager]

    /**
     * Returns the result that this token editor should have if it is not able to recognize a token.
     *
     * You must override this method if the result type of your editor is not nullable.
     * Otherwise the default implementation will throw an [IllegalStateException].
     * If the default implementation is called on a token editor whose result type is nullable it just returns null.
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun defaultResult(): R =
        if (resultType.isMarkedNullable) null as R
        else error("TokenEditor ${this::class}: non-nullable result type and defaultResult() was not overwritten")

    override fun compile(token: String): R = defaultResult()

    private fun tryCompile(text: String) =
        context.executeSafely("compiling item", ::defaultResult) { compile(text) }

    override fun viewAdded(view: V) {
        view.displayText(text.now)
    }

    override fun createSnapshot(): Snapshot<*> = Snap()

    override fun supportsCopyPaste(): Boolean = true

    /**
     * Set the text of this editor, such that the result is automatically updated
     */
    fun setText(newText: String) {
        if (undo.isActive) {
            val edit = TextEdit(virtualize(), text.now, newText)
            undo.record(edit)
        }
        _text.now = newText
        views { displayText(newText) }
        _result.set(tryCompile(newText))
    }

    /**
     * Set the text of this editor to the completion text of the given [completion] and then compile the completed item.
     */
    fun complete(completion: Completion<*>) {
        val t = completion.completionText
        val edit = TextEdit(virtualize(), text.now, t)
        undo.record(edit)
        _text.now = t
        views { displayText(t) }
        @Suppress("UNCHECKED_CAST")
        val res = resultType.jvmErasure.safeCast(completion.item) as R?
            ?: tryCompile(completion.completionText)
            ?: defaultResult()
        _result.set(res)
    }

    private class TextEdit(
        private val editor: VirtualEditor<TokenEditor<*, *>>,
        private val old: String,
        private val new: String
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.get().setText(new)
        }

        override fun doUndo() {
            editor.get().setText(old)
        }

        override val actionDescription: String
            get() = "Editing"

        override fun mergeWith(other: Edit): Edit? =
            if (other !is TextEdit || other.editor !== this.editor) null
            else TextEdit(editor, this.old, other.new)
    }

    private class Snap : Snapshot<TokenEditor<*, *>>() {
        private lateinit var text: String

        override fun doRecord(original: TokenEditor<*, *>) {
            text = original.text.now
        }

        override fun reconstructObject(original: TokenEditor<*, *>) {
            original.setText(text)
        }

        override fun encode(builder: JsonObjectBuilder) {
            builder.put("text", JsonPrimitive(this.text))
        }

        override fun decode(element: JsonObject) {
            text = element.getValue("text").string
        }
    }
}