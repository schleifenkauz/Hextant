/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.base.AbstractEditor
import hextant.bundle.CorePermissions.Public
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.core.editable.EditableToken
import hextant.core.view.TokenEditorView
import hextant.runLater
import hextant.undo.*
import reaktive.value.now

/**
 * An editor for tokens
 */
abstract class TokenEditor<E : EditableToken<*>, V : TokenEditorView>(
    editable: E,
    private val context: Context,
    private val completer: Completer<String> = NoCompleter
) : AbstractEditor<E, V>(editable, context) {
    private val undo = context[Public, UndoManager]

    override fun viewAdded(view: V) {
        super.viewAdded(view)
        view.onGuiThread { view.displayText(editable.text.now) }
    }

    /**
     * Set the text on the platform thread and notify the views
     */
    @Synchronized fun setText(new: String) {
        context.runLater {
            if (new != editable.text.now) {
                val edit = doSetText(new)
                if (editable.edited.now == null) {
                    suggestCompletions(new)
                }
                undo.push(edit)
            }
        }
    }

    private fun suggestCompletions(new: String) {
        val completions = completer.completions(new)
        views {
            displayCompletions(completions)
        }
    }

    @Synchronized fun suggestCompletions() {
        suggestCompletions(editable.text.now)
    }

    private fun doSetText(new: String): TextEdit {
        val edit = TextEdit(this, editable.text.get(), new)
        context.runLater {
            editable.text.set(new)
            views { displayText(new) }
        }
        return edit
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
}