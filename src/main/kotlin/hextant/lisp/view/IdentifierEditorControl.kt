/**
 *@author Nikolaus Knop
 */

package hextant.lisp.view

import hextant.Context
import hextant.core.editable.EditableToken
import hextant.core.expr.view.FXTokenEditorView
import hextant.getEditor
import hextant.lisp.editor.IdentifierEditor

class IdentifierEditorControl(editable: EditableToken<Any>, context: Context) : FXTokenEditorView(editable, context) {
    init {
        val editor = context.getEditor(editable) as IdentifierEditor
        editor.addView(this)
        initialize(editable, editor, context)
    }
}