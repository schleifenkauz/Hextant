/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TextEditorView
import hextant.lisp.editable.EditableIdentifier

class IdentifierEditor(editable: EditableIdentifier, context: Context) :
    TokenEditor<EditableIdentifier, TextEditorView>(editable, context)