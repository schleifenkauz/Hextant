/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableSubNode
import hextant.*
import hextant.base.ParentEditor

class SubNodeEditor(editable: EditableSubNode, context: Context) :
    ParentEditor<EditableSubNode, EditorView>(editable, context) {
    init {
        context.getEditor(editable.name).moveTo(this)
        context.getEditor(editable.type).moveTo(this)
    }
}