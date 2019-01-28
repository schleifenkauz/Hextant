/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.expr.view.TextEditorView
import hextant.lisp.editable.EditableIntLiteral

class IntLiteralEditor(editable: EditableIntLiteral, context: Context) :
    TokenEditor<EditableIntLiteral, TextEditorView>(editable, context)