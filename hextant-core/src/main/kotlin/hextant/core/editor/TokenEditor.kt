/**
 *@author Nikolaus Knop
 */

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
abstract class TokenEditor<out R, in V : TokenEditorView>(context: Context, strategy: ResultStrategy<R>? = null) :
    AbstractEditor<R, V>(context, strategy), TokenType<R> {
    private sealed class Compilable {
        data class Completed(val completion: Completion<Any>) : Compilable()
        data class Text(val input: String) : Compilable()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val compiler = GlobalScope.actor<Compilable>(Dispatchers.Main, capacity = Channel.CONFLATED) {
        for (compilable in channel) {
            val res = when (compilable) {
                is Completed -> {
                    val comp = compilable.completion
                    resultStrategy.chooseFrom(tryCompile(comp.item), tryCompile(comp.completionText))
                }
                is Text      -> tryCompile(compilable.input)
            }
            _result.set(res)
        }
    }

    constructor(context: Context, text: String, resultStrategy: ResultStrategy<R>? = null) : this(
        context,
        resultStrategy
    ) {
        withoutUndo { setText(text) }
    }

    private val _result = reactiveVariable(runBlocking { tryCompile("") })

    override val result: ReactiveValue<R> get() = _result

    private var _text = reactiveVariable("")

    /**
     * A [ReactiveValue] holding the current textual content of this editor
     */
    val text: ReactiveString get() = _text

    private val undo = context[UndoManager]

    /**
     * Make a result from the given completion item.
     *
     * The default implementation returns the default result of the [resultStrategy].
     */
    protected open fun compile(item: Any): R = resultStrategy.default()

    override fun compile(token: String): R = resultStrategy.default()

    private suspend fun tryCompile(item: Any): R =
        context.executeSafely("compiling item", resultStrategy.default()) {
            withContext(Dispatchers.Default) { compile(item) }
        }

    private suspend fun tryCompile(text: String): R =
        context.executeSafely("compiling item", resultStrategy.default()) {
            withContext(Dispatchers.Default) { compile(text) }
        }

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
        GlobalScope.launch { compiler.send(Text(newText)) }
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