/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.expr.editable.EditableSum
import hextant.expr.edited.Expr
import reaktive.value.now

class SumEditor(
    sum: EditableSum,
    context: Context
) : ExprEditor,
    AbstractEditor<EditableSum, EditorView>(sum, context) {
    override val expr: Expr?
        get() = editable.result.now.orNull()
}