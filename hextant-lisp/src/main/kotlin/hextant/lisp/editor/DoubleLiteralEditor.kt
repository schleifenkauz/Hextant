/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.editable.EditableDoubleLiteral

class DoubleLiteralEditor(editable: EditableDoubleLiteral, context: Context) :
    TokenEditor<EditableDoubleLiteral, TokenEditorView>(editable, context)