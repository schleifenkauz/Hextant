/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TextEditorView
import hextant.expr.editable.EditableOperator

class OperatorEditor(
    editable: EditableOperator,
    context: Context
) : TokenEditor<EditableOperator, TextEditorView>(editable, context)