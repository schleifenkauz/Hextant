/**
 *@author Nikolaus Knop
 */

package hackus.editor

import hackus.editable.EditableFQName
import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView

class FQNameEditor(editable: EditableFQName, context: Context) :
    TokenEditor<EditableFQName, TokenEditorView>(editable, context)