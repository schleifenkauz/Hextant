/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.EditorView
import hextant.base.AbstractEditor
import hextant.lisp.editable.EditableApply

class ApplyEditor(editable: EditableApply, context: Context) :
    AbstractEditor<EditableApply, EditorView>(editable, context)