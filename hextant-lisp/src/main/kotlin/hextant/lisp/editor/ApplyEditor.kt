/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.*
import hextant.base.ParentEditor
import hextant.lisp.editable.EditableApply

class ApplyEditor(editable: EditableApply, context: Context) :
    ParentEditor<EditableApply, EditorView>(editable, context) {
    init {
        context.getEditor(editable.editableExpressions).moveTo(this)
    }

    override fun accepts(child: Editor<*>): Boolean = true
}