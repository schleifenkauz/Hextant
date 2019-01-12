/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.expr.view.TextEditorView
import hextant.lisp.editable.EditableDoubleLiteral

class DoubleLiteralEditor(editable: EditableDoubleLiteral, context: Context) :
    TokenEditor<EditableDoubleLiteral, TextEditorView>(editable, context)