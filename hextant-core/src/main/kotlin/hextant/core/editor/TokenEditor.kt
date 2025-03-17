/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.completion.Completion
import hextant.core.Editor
import hextant.core.view.ListEditorControl
import hextant.core.view.TokenEditorView
import hextant.serial.IndexAccessor
import hextant.undo.AbstractEdit
import hextant.undo.Edit
import hextant.undo.UndoManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.safeCast
import kotlin.reflect.jvm.jvmErasure

/**
 * A token editor transforms text to tokens.
 * When setting the text it is automatically compiled to a token.
 */
@Serializable
abstract class TokenEditor<out R, in V : TokenEditorView> : AbstractEditor<R, V>(), TokenType<R> {
    private lateinit var _text: ReactiveVariable<String>

    //TODO this is really ugly, do we really need this?
    @Transient
    private val resultType = this::class.memberFunctions.first { f -> f.name == "compile" }.returnType

    @Transient
    private lateinit var _result: ReactiveVariable<R>

    final override val result: ReactiveValue<R> get() = _result

    /**
     * A [ReactiveValue] holding the current textual content of this editor
     */
    val text: ReactiveString get() = _text

    override fun viewAdded(view: V) {
        view.displayText(text.now)
    }

    override fun setupDefaultState() {
        setInitialText("")
    }

    fun setInitialText(text: String) {
        _text = reactiveVariable(text)
    }

    override fun doInitialize() {
        _result = reactiveVariable(compile(text.now))
    }

    override fun supportsCopyPaste(): Boolean = true

    override fun paste(editor: Editor<*>): Boolean {
        if (this::class.isInstance(editor)) {
            val token = editor as TokenEditor<*, *>
            setText(token.text.now)
            return true
        }
        return false
    }

    /**
     * Set the text of this editor, such that the result is automatically updated
     */
    fun setText(newText: String) {
        if (newText.endsWith(",")) {
            val listEditor = parent as ListEditor<*, *>
            val (index) = accessor as IndexAccessor
            val addWithComma = listEditor.viewManager.listeners.any { v ->
                v is ListEditorControl && v.arguments[ListEditorControl.ADD_WITH_COMMA]
            }
            if (addWithComma) {
                listEditor.addAt(index + 1)
                notifyViews { setText(newText.removeSuffix(",")) }
                return
            }
        }
        if (context[UndoManager].isActive) {
            val edit = TextEdit(this, text.now, newText)
            context[UndoManager].record(edit)
        }
        _text.now = newText
        notifyViews { displayText(newText) }
        _result.set(compile(newText))
    }

    /**
     * Set the text of this editor to the completion text of the given [completion] and then compile the completed item.
     */
    fun complete(completion: Completion<*>) {
        val t = completion.completionText
        val edit = TextEdit(this, text.now, t)
        context[UndoManager].record(edit)
        _text.now = t
        notifyViews { displayText(t) }
        @Suppress("UNCHECKED_CAST")
        val res = resultType.jvmErasure.safeCast(completion.item) as R?
            ?: compile(completion.completionText)
        _result.set(res)
    }

    private class TextEdit(
        private val editor: TokenEditor<*, *>,
        private val old: String,
        private val new: String
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.setText(new)
        }

        override fun doUndo() {
            editor.setText(old)
        }

        override val actionDescription: String
            get() = "Editing"

        override fun mergeWith(other: Edit): Edit? =
            if (other !is TextEdit || other.editor !== this.editor) null
            else TextEdit(editor, this.old, other.new)
    }
}