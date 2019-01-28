/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.expr.view.TextEditorView
import hextant.sample.editable.EditableIntLiteral

class IntLiteralEditor(
    editable: EditableIntLiteral,
    context: Context
) : TokenEditor<EditableIntLiteral, TextEditorView>(editable, context)