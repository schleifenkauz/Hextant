/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.expr.edited.Sum
import reaktive.value.binding.map

class SumEditor(
    context: Context,
    exprs: ExprListEditor
) : AbstractEditor<Sum, Any>(context), ExprEditor<Sum> {
    val expressions = exprs.moveTo(context)

    constructor(context: Context) : this(context, ExprListEditor(context))

    override val result: EditorResult<Sum> = exprs.result.map { exprs -> exprs.map(::Sum) }

    override fun copyForImpl(context: Context): ExprEditor<Sum> = SumEditor(context, expressions.copyFor(context))
}