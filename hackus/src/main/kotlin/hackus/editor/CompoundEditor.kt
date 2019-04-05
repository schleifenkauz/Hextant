/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableCompound
import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor

class CompoundEditor(editable: EditableCompound, context: Context) :
    AbstractEditor<EditableCompound, EditorView>(editable, context)