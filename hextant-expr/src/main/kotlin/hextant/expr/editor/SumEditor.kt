/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.expr.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.expr.edited.Sum
import reaktive.value.binding.map

class SumEditor(
    context: Context,
    val expressions: ExprListEditor
) : AbstractEditor<Sum, Any>(context), ExprEditor<Sum> {
    constructor(context: Context) : this(context, ExprListEditor(context))

    override val result: EditorResult<Sum> = expressions.result.map { exprs -> exprs.map(::Sum) }

    override fun copyFor(context: Context): ExprEditor<Sum> = SumEditor(context, expressions.copyFor(context))
}