/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableTerminal
import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor

class TerminalEditor(editable: EditableTerminal, context: Context) :
    AbstractEditor<EditableTerminal, EditorView>(editable, context)