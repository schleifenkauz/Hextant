/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.expr.view.TextEditorView
import org.nikok.hextant.core.undo.*
import org.nikok.hextant.get
import org.nikok.reaktive.value.now

/**
 * An editor for tokens
 */
abstract class TokenEditor<E : EditableToken<*>, V : TextEditorView>(
    editable: E,
    private val platform: HextantPlatform
) : AbstractEditor<E, V>(editable, platform) {
    private val undo = platform[UndoManagerFactory].get(AContext)

    override fun viewAdded(view: V) {
        view.onGuiThread { view.displayText(editable.text.now) }
    }

    /**
     * Set the text on the platform thread and notify the views
     */
    fun setText(new: String) {
        if (new != editable.text.now) {
            val edit = doSetText(new)
            undo.push(edit)
        }
    }

    private fun doSetText(new: String): TextEdit {
        val edit = TextEdit(this, editable.text.get(), new)
        platform.runLater {
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