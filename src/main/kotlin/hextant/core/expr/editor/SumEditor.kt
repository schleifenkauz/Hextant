/**
 *@author Nikolaus Knop
 */

package hextant.core.expr.editor

import hextant.Context
import hextant.EditorView
import hextant.core.base.AbstractEditor
import hextant.core.expr.editable.EditableSum
import hextant.core.expr.edited.Expr
import org.nikok.reaktive.value.now

class SumEditor(
    sum: EditableSum,
    context: Context
) : ExprEditor,
    AbstractEditor<EditableSum, EditorView>(sum, context) {
    override val expr: Expr?
        get() = editable.edited.now
}