/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableDefinition
import hextant.*
import hextant.base.ParentEditor

class DefinitionEditor(editable: EditableDefinition, context: Context) :
    ParentEditor<EditableDefinition, EditorView>(editable, context) {
    init {
        context.getEditor(editable.name).moveTo(this)
        context.getEditor(editable.rightSide).moveTo(this)
    }
}