/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor
import hextant.sample.editable.EditableIntOperatorApplication

class IntOperatorApplicationEditor(
    editable: EditableIntOperatorApplication,
    context: Context
) : AbstractEditor<EditableIntOperatorApplication, EditorView>(editable, context)