/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableTerminal
import hextant.*
import hextant.base.ParentEditor

class TerminalEditor(editable: EditableTerminal, context: Context) :
    ParentEditor<EditableTerminal, EditorView>(editable, context) {
    init {
        context.getEditor(editable.fqName).moveTo(this)
    }

    override fun accepts(child: Editor<*>) = child is FQNameEditor
}