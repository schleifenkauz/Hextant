/**
 *@author Nikolaus Knop
 */

package hextant.lisp.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.lisp.editable.EditableIdentifier

class IdentifierEditor(editable: EditableIdentifier, context: Context) :
    TokenEditor<EditableIdentifier, TokenEditorView>(editable, context)