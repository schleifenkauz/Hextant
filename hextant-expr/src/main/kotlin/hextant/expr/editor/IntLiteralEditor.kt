/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.Expr
import hextant.expr.view.IntLiteralEditorView
import hextant.orNull
import reaktive.value.now

class IntLiteralEditor(
    editable: EditableIntLiteral,
    context: Context
) : TokenEditor<EditableIntLiteral, IntLiteralEditorView>(editable, context),
    ExprEditor {
    override val expr: Expr?
        get() = editable.result.now.orNull()
}