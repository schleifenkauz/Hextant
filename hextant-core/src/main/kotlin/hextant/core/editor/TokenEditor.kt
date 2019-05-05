/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.core.TokenType
import hextant.core.view.TokenEditorView
import hextant.undo.*
import reaktive.value.reactiveVariable

abstract class TokenEditor<out R : Any>(context: Context) : AbstractEditor<R, TokenEditorView>(context), TokenType<R> {
    private val _result = reactiveVariable(this.compile(""))

    override val result: EditorResult<R> get() = _result

    private var _text = ""

    val text get() = _text

    private val undo = context[UndoManager]

    override fun viewAdded(view: TokenEditorView) {
        view.displayText(text)
    }

    fun setText(newText: String) {
        val edit = TextEdit(this, text, newText)
        doSetText(newText)
        undo.push(edit)
    }

    private fun doSetText(newText: String) {
        _text = newText
        _result.set(compile(text))
        views { displayText(newText) }
    }


    private class TextEdit(private val editor: TokenEditor<*>, private val old: String, private val new: String) :
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
        fun <R : Any> forTokenType(
            type: TokenType<R>,
            context: Context
        ) = object : TokenEditor<R>(context) {
            override fun compile(token: String): CompileResult<R> = type.compile(token)
        }
    }
}