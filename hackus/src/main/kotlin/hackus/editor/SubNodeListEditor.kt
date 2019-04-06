/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableSubNode
import hackus.editable.EditableSubNodeList
import hextant.Context
import hextant.Editor
import hextant.core.list.EditableList
import hextant.core.list.ListEditor

class SubNodeListEditor(
    list: EditableSubNodeList,
    context: Context
) : ListEditor<EditableSubNode, EditableSubNodeList>(list, context) {
    override fun createNewEditable(): EditableSubNode = EditableSubNode()

    override fun accepts(child: Editor<*>): Boolean = child is SubNodeEditor
}