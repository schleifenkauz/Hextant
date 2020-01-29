/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.CompoundEditor
import hextant.expr.edited.Sum

class SumEditor(
    context: Context
) : CompoundEditor<Sum>(context), ExprEditor<Sum> {
    val expressions by child(ExprListEditor(context))

    override val result: EditorResult<Sum> = expressions.result.mapResult(::Sum)
}