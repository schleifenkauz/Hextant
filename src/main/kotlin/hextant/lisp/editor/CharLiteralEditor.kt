/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.expr.view.TextEditorView
import hextant.lisp.editable.EditableCharLiteral

class CharLiteralEditor(editable: EditableCharLiteral, context: Context) :
    TokenEditor<EditableCharLiteral, TextEditorView>(editable, context)