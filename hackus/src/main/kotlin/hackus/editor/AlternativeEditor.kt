/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableAlternative
import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor

class AlternativeEditor(editable: EditableAlternative, context: Context) :
    AbstractEditor<EditableAlternative, EditorView>(editable, context)