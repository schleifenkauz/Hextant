/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor
import hextant.lisp.editable.EditableGetVal

class GetValEditor(editable: EditableGetVal, context: Context) :
    AbstractEditor<EditableGetVal, EditorView>(editable, context)