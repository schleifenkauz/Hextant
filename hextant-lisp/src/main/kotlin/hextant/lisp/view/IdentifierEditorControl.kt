/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.core.editable.EditableToken
import hextant.core.view.FXTokenEditorView
import hextant.getEditor
import hextant.lisp.editor.IdentifierEditor

class IdentifierEditorControl(
    editable: EditableToken<Any>,
    context: Context,
    args: Bundle
) : FXTokenEditorView(editable, context, args) {
    init {
        val editor = context.getEditor(editable) as IdentifierEditor
        initialize(editable, editor, context)
    }
}