/**
 * @author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.base.AbstractEditor
import hextant.core.expr.view.TextEditorView
import hextant.lisp.editable.EditableGetVal

class GetValEditor(editable: EditableGetVal, context: Context) :
    AbstractEditor<EditableGetVal, TextEditorView>(editable, context)