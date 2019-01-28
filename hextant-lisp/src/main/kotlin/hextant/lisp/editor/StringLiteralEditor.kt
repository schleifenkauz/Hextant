/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.expr.view.TextEditorView
import hextant.lisp.editable.EditableStringLiteral

class StringLiteralEditor(editable: EditableStringLiteral, context: Context) :
    TokenEditor<EditableStringLiteral, TextEditorView>(editable, context)