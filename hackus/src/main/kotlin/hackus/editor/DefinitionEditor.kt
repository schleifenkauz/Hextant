/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableDefinition
import hextant.Context
import hextant.EditorView
import hextant.base.ParentEditor

class DefinitionEditor(editable: EditableDefinition, context: Context) :
    ParentEditor<EditableDefinition, EditorView>(editable, context) {
    init {
        addChildren(editable.name, editable.rightSide)
    }
}