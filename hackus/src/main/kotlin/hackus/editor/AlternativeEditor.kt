/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableAlternative
import hextant.*
import hextant.base.ParentEditor

class AlternativeEditor(editable: EditableAlternative, context: Context) :
    ParentEditor<EditableAlternative, EditorView>(editable, context) {
    init {
        context.getEditor(editable.alternatives).moveTo(this)
    }

    override fun accepts(child: Editor<*>): Boolean = child is FQNameListEditor
}