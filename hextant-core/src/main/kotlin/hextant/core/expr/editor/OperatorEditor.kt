/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.expr.editable.EditableOperator
import hextant.core.expr.view.TextEditorView

class OperatorEditor(
    editable: EditableOperator,
    context: Context
) : TokenEditor<EditableOperator, TextEditorView>(editable, context)