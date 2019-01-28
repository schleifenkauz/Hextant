/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.core.editable.EditableToken
import hextant.expr.view.FXTokenEditorView
import hextant.getEditor
import hextant.lisp.editor.IdentifierEditor

class IdentifierEditorControl(editable: EditableToken<Any>, context: Context) : FXTokenEditorView(editable, context) {
    init {
        val editor = context.getEditor(editable) as IdentifierEditor
        initialize(editable, editor, context)
    }
}