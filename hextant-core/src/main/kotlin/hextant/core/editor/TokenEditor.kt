/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.TokenType
import hextant.core.view.TokenEditorView
import hextant.undo.*
import kserial.*
import reaktive.value.*

/**
 * A token editor transforms text to tokens.
 * When setting the text it is automatically compiled to a token.
 */
abstract class TokenEditor<out R : Any, in V : TokenEditorView>(context: Context) :
    AbstractEditor<R, V>(context), TokenType<R>, Serializable {
    constructor(context: Context, text: String) : this(context) {
        doSetText(text)
    }

    private val _result = reactiveVariable(this.compile(""))

    override val result: EditorResult<R> get() = _result

    private var _text = reactiveVariable("")

    /**
     * A [ReactiveValue] holding the current textual content of this editor
     */
    val text: ReactiveString get() = _text

    private val undo = context[UndoManager]

    override fun viewAdded(view: V) {
        view.displayText(text.now)
    }

    private val constructor by lazy { javaClass.getConstructor(Context::class.java) }

    override fun copyForImpl(context: Context): Editor<R> {
        val copy = constructor.newInstance(context)
        copy.doSetText(this.text.now)
        return copy
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeString(text.now)
    }

    override fun deserialize(input: Input, context: SerialContext) {
        val txt = input.readString()
        setText(txt)
    }

    /**
     * Set the text of this editor, such that the result is automatically updated
     */
    fun setText(newText: String) {
        val edit = TextEdit(this, text.now, newText)
        doSetText(newText)
        undo.push(edit)
    }

    private fun doSetText(newText: String) {
        _text.now = newText
        _result.set(compile(text.now))
        views { displayText(newText) }
    }

    private class TextEdit(private val editor: TokenEditor<*, *>, private val old: String, private val new: String) :
        AbstractEdit() {
        override fun doRedo() {
            editor.doSetText(new)
        }

        override fun doUndo() {
            editor.doSetText(old)
        }

        override val actionDescription: String
            get() = "Editing"

        override fun mergeWith(other: Edit): Edit? =
            if (other !is TextEdit || other.editor !== this.editor) null
            else TextEdit(editor, this.old, other.new)
    }

    companion object {
        /**
         * Return a [TokenEditor] which delegates text compilation to the given token [type]
         */
        fun <R : Any> forTokenType(
            type: TokenType<R>,
            context: Context
        ) = object : TokenEditor<R, TokenEditorView>(context) {
            override fun compile(token: String): CompileResult<R> = type.compile(token)
        }
    }
}