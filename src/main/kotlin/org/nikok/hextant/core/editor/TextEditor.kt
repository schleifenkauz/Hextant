/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.view.TextEditorView
import org.nikok.reaktive.value.now

class TextEditor(editableText: EditableText) : AbstractEditor<EditableText, TextEditorView>(editableText) {
    override fun viewAdded(view: TextEditorView) {
        view.onGuiThread { view.displayText(editable.text.now) }
    }

    fun setText(new: String) {
        HextantPlatform.runLater {
            editable.text.set(new)
            views { displayText(new) }
        }
    }
}