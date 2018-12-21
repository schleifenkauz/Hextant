/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.Context
import hextant.core.base.AbstractEditor
import hextant.core.expr.editable.EditableOperatorApplication
import hextant.core.expr.edited.Expr
import hextant.core.expr.view.FXOperatorApplicationEditorView
import org.nikok.reaktive.value.now

class OperatorApplicationEditor(
    editable: EditableOperatorApplication,
    context: Context
) : AbstractEditor<EditableOperatorApplication, FXOperatorApplicationEditorView>(editable, context), ExprEditor {
    override val expr: Expr?
        get() = editable.edited.now
}