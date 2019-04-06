/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableSubNode
import hextant.Context
import hextant.EditorView
import hextant.base.ParentEditor

class SubNodeEditor(editable: EditableSubNode, context: Context) :
    ParentEditor<EditableSubNode, EditorView>(editable, context) {
    init {
        addChildren(editable.name, editable.type)
    }
}