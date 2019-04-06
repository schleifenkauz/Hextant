/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableFQName
import hackus.editable.EditableFQNameList
import hextant.Context
import hextant.Editor
import hextant.core.list.EditableList
import hextant.core.list.ListEditor

class FQNameListEditor(
    list: EditableFQNameList,
    context: Context
) : ListEditor<EditableFQName, EditableFQNameList>(list, context) {
    override fun createNewEditable(): EditableFQName = EditableFQName()

    override fun accepts(child: Editor<*>): Boolean = child is FQNameEditor
}