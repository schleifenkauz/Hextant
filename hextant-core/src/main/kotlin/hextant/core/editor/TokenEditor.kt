/**
 *@author Nikolaus Knop
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package hextant.core.editor

import hextant.completion.Completion
import hextant.context.*
import hextant.core.editor.TokenEditor.Compilable.Completed
import hextant.core.editor.TokenEditor.Compilable.Text
import hextant.core.view.TokenEditorView
import hextant.serial.*
import hextant.undo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.json.*
import reaktive.value.*

/**
 * A token editor transforms text to tokens.
 * When setting the text it is automatically compiled to a token.
 */
abstract class TokenEditor<out R, in V : TokenEditorView>(context: Context) : AbstractEditor<R, V>(context),
                                                                              TokenType<R> {
    private sealed class Compilable {
        data class Completed(val completion: Completion<Any>) : Compilable()
        data class Text(val input: String) : Compilable()
    }

    private val compiler = GlobalScope.actor<Compilable>(Dispatchers.Main, capacity = Channel.CONFLATED) {
        for (compilable in channel) {
            val res = when (compilable) {
                is Completed -> {
                    val comp = compilable.completion
                    tryWrap(comp.completion) ?: tryWrap(comp.completionText)
                }
                is Text -> tryWrap(compilable.input)
            }
            _result.set(res ?: defaultResult())
        }
    }

    constructor(context: Context, text: String) : this(context) {
        withoutUndo { setText(text) }
    }

    private val resultType = getTypeArgument(TokenEditor::class, 0)

    private val _result = reactiveVariable(runBlocking { tryWrap("") ?: defaultResult() })

    override val result: ReactiveValue<R> get() = _result

    private var _text = reactiveVariable("")

    /**
     * A [ReactiveValue] holding the current textual content of this editor
     */
    val text: ReactiveString get() = _text

    private val undo = context[UndoManager]

    /**
     * Make a result from the given completion item or return `null` if this is not possible.
     */
    protected open fun wrap(item: Any): R? = null

    /**
     * The default implementation returns [defaultResult].
     */
    override fun wrap(token: String): R = defaultResult()

    /**
     * Returns a default result that is used when no valid result can be produced for a token.
     */
    protected open fun defaultResult(): R =
        @Suppress("UNCHECKED_CAST")
        if (resultType.isMarkedNullable) null as R
        else error("default implementation of defaultResult() only works for nullable result types")

    private suspend fun tryWrap(item: Any): R? =
        context.executeSafely("compiling item", null) { withContext(Dispatchers.Default) { wrap(item) } }

    private suspend fun tryWrap(text: String): R? =
        context.executeSafely("compiling item", null) { withContext(Dispatchers.Default) { wrap(text) } }

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
            undo.push(edit)
        }
        _text.now = newText
        views { displayText(newText) }
        GlobalScope.launch { compiler.send(Text(newText)) }
    }

    /**
     * Set the text of this editor to the completion text of the given [completion] and then compile the completed item.
     */
    fun complete(completion: Completion<*>) {
        val t = completion.completionText
        val edit = TextEdit(virtualize(), text.now, t)
        undo.push(edit)
        _text.now = t
        views { displayText(t) }
        GlobalScope.launch { compiler.send(Completed(completion)) }
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

        override fun reconstruct(original: TokenEditor<*, *>) {
            original.setText(text)
        }

        override fun JsonObjectBuilder.encode() {
            put("text", JsonPrimitive(text))
        }

        override fun decode(element: JsonObject) {
            text = element.getValue("text").string
        }
    }
}