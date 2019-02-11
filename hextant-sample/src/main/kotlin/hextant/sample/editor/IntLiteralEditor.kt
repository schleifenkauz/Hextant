/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.sample.editable.EditableIntLiteral

class IntLiteralEditor(
    editable: EditableIntLiteral,
    context: Context
) : TokenEditor<EditableIntLiteral, TokenEditorView>(editable, context)