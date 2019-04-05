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
        addChildren(editable.fqName)
    }

    override fun accepts(child: Editor<*>) = child is FQNameEditor
}