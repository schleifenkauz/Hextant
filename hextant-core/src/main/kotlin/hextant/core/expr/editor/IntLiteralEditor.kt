/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.expr.editable.EditableIntLiteral
import hextant.core.expr.edited.Expr
import hextant.core.expr.view.IntLiteralEditorView
import org.nikok.reaktive.value.now

class IntLiteralEditor(
    editable: EditableIntLiteral,
    context: Context
) : TokenEditor<EditableIntLiteral, IntLiteralEditorView>(editable, context), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now
}