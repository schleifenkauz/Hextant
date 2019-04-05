/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableDefinition
import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor

class DefinitionEditor(editable: EditableDefinition, context: Context) :
    AbstractEditor<EditableDefinition, EditorView>(editable, context)