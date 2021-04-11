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
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure

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

    private val resultType = this::class.memberFunctions.first { it.name == "defaultResult" }.returnType

    @OptIn(ObsoleteCoroutinesApi::class)
    private val compiler = GlobalScope.actor<Compilable>(Dispatchers.Default, capacity = Channel.CONFLATED) {
        for (compilable in channel) {
            val res = when (compilable) {
                is Completed -> {
                    val comp = compilable.completion
                    tryCompile(comp.item).takeUnless { it == defaultResult() } ?: tryCompile(comp.completionText)
                }
                is Text      -> tryCompile(compilable.input)
            }
            _result.set(res)
        }
    }

    constructor(context: Context, text: String) : this(context) {
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

    /**
     * Make a result from the given completion item.
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun compile(item: Any): R = resultType.jvmErasure.safeCast(item) as R ?: defaultResult()

    override fun compile(token: String): R = defaultResult()

    private suspend fun tryCompile(item: Any): R =
        context.executeSafely("compiling item", defaultResult()) { withContext(Dispatchers.Default) { compile(item) } }

    private suspend fun tryCompile(text: String): R =
        context.executeSafely("compiling item", defaultResult()) { withContext(Dispatchers.Default) { compile(text) } }

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