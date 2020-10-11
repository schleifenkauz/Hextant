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
import hextant.serial.VirtualEditor
import hextant.serial.virtualize
import hextant.undo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import reaktive.value.*
import validated.*
import validated.reaktive.ReactiveValidated

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
                    tryCompile(comp.completion).orElse { tryCompile(comp.completionText) }
                }
                is Text -> tryCompile(compilable.input)
            }
            _result.set(res)
        }
    }

    constructor(context: Context, text: String) : this(context) {
        withoutUndo { setText(text) }
    }

    private val _result by lazy { reactiveVariable(runBlocking { tryCompile("") }) }

    override val result: ReactiveValidated<R> get() = _result

    private var _text = reactiveVariable("")

    /**
     * A [ReactiveValue] holding the current textual content of this editor
     */
    val text: ReactiveString get() = _text

    private val undo = context[UndoManager]

    /**
     * Make a result from the given completion item.
     */
    protected open fun compile(item: Any): Validated<R> = invalidComponent()

    override fun compile(token: String): Validated<R> = invalidComponent()

    private suspend fun tryCompile(item: Any): Validated<R> =
        context.executeSafely("compiling item", invalidComponent) { withContext(Dispatchers.Default) { compile(item) } }

    private suspend fun tryCompile(text: String): Validated<R> =
        context.executeSafely("compiling item", invalidComponent) { withContext(Dispatchers.Default) { compile(text) } }

    override fun viewAdded(view: V) {
        view.displayText(text.now)
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

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

    private class Snapshot(original: TokenEditor<*, *>) : EditorSnapshot<TokenEditor<*, *>>(original) {
        private val text = original.text.now

        override fun reconstruct(editor: TokenEditor<*, *>) {
            editor.setText(text)
        }
    }
}