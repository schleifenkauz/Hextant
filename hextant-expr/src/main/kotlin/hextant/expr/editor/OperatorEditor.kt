/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.editable.EditableOperator

class OperatorEditor(
    editable: EditableOperator,
    context: Context
) : TokenEditor<EditableOperator, TokenEditorView>(editable, context)