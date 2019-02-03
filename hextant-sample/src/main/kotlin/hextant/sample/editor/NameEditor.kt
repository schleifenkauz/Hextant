/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TextEditorView
import hextant.sample.editable.EditableName

class NameEditor(
    editable: EditableName,
    context: Context
) : TokenEditor<EditableName, TextEditorView>(editable, context)
