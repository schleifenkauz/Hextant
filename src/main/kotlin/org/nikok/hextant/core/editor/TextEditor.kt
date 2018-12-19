/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.Context
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.expr.view.TextEditorView
import org.nikok.hextant.runLater
import org.nikok.reaktive.value.now

class TextEditor(
    editableText: EditableText,
    private val context: Context
) : AbstractEditor<EditableText, TextEditorView>(editableText, context) {
    override fun viewAdded(view: TextEditorView) {
        view.onGuiThread { view.displayText(editable.text.now) }
    }

    fun setText(new: String) {
        context.runLater {
            editable.text.set(new)
            views { displayText(new) }
        }
    }
}