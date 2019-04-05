/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableJIdent
import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView

class JIdentEditor(editable: EditableJIdent, context: Context) : TokenEditor<EditableJIdent, TokenEditorView>(editable, context)