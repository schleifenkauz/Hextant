/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.view.TextEditorView
import org.nikok.reaktive.value.observe

class TextEditor(private val editableText: EditableText, view: TextEditorView)
    : AbstractEditor<EditableText>(editableText, view) {
    private val textObserver = editableText.text.observe("observe text for editor", view::displayText)

    fun dispose() {
        textObserver.kill()
    }

    fun setText(new: String) {
        editableText.text.set(new)
    }
}