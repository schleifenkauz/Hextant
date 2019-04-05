/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableSubNode
import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor

class SubNodeEditor(editable: EditableSubNode, context: Context) :
    AbstractEditor<EditableSubNode, EditorView>(editable, context)