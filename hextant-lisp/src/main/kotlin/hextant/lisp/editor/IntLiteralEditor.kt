/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.editable.EditableIntLiteral

class IntLiteralEditor(editable: EditableIntLiteral, context: Context) :
    TokenEditor<EditableIntLiteral, TokenEditorView>(editable, context)