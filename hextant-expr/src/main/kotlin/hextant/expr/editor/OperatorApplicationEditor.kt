/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.ParentEditor
import hextant.expr.editable.EditableExpr
import hextant.expr.editable.EditableOperatorApplication
import hextant.expr.edited.Expr
import hextant.expr.view.FXOperatorApplicationEditorView
import reaktive.value.now

class OperatorApplicationEditor(
    editable: EditableOperatorApplication,
    context: Context
) : ParentEditor<EditableOperatorApplication, FXOperatorApplicationEditorView>(editable, context),
    ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now

    override fun accepts(child: Editor<*>): Boolean = child is OperatorEditor || child.editable is EditableExpr<*>

    init {
        context.getEditor(editable.editableOp1).moveTo(this)
        context.getEditor(editable.editableOperator).moveTo(this)
        context.getEditor(editable.editableOp2).moveTo(this)
    }
}