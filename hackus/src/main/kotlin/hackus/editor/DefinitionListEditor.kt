/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableDefinition
import hackus.editable.EditableDefinitionList
import hextant.Context
import hextant.Editor
import hextant.core.list.EditableList
import hextant.core.list.ListEditor

class DefinitionListEditor(
    list: EditableDefinitionList,
    context: Context
) : ListEditor<EditableDefinition, EditableDefinitionList>(list, context) {
    override fun createNewEditable(): EditableDefinition = EditableDefinition()

    override fun accepts(child: Editor<*>): Boolean = child is DefinitionEditor
}