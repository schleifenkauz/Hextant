/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.editable.EditableStringLiteral

class StringLiteralEditor(editable: EditableStringLiteral, context: Context) :
    TokenEditor<EditableStringLiteral, TokenEditorView>(editable, context)