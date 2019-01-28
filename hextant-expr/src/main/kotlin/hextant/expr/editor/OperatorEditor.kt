/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.expr.editable.EditableOperator
import hextant.expr.view.TextEditorView

class OperatorEditor(
    editable: EditableOperator,
    context: Context
) : TokenEditor<EditableOperator, TextEditorView>(editable, context)