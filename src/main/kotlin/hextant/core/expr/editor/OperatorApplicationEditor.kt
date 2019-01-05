/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.*
import hextant.base.ParentEditor
import hextant.core.expr.editable.EditableExpr
import hextant.core.expr.editable.EditableOperatorApplication
import hextant.core.expr.edited.Expr
import hextant.core.expr.view.FXOperatorApplicationEditorView
import org.nikok.reaktive.value.now

class OperatorApplicationEditor(
    editable: EditableOperatorApplication,
    context: Context
) : ParentEditor<EditableOperatorApplication, FXOperatorApplicationEditorView>(editable, context), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now

    override fun accepts(child: Editor<*>): Boolean = child is OperatorEditor || child.editable is EditableExpr<*>

    init {
        context.getEditor(editable.editableOp1).moveTo(this)
        context.getEditor(editable.editableOperator).moveTo(this)
        context.getEditor(editable.editableOp2).moveTo(this)
    }
}