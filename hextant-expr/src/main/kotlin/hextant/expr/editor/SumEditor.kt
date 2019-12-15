/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.expr.edited.Sum
import kserial.CompoundSerializable
import reaktive.value.binding.map

class SumEditor(
    context: Context,
    exprs: ExprListEditor
) : AbstractEditor<Sum, Any>(context), ExprEditor<Sum>, CompoundSerializable {
    val expressions = exprs.moveTo(context)

    constructor(context: Context) : this(context, ExprListEditor(context))

    override val result: EditorResult<Sum> = exprs.result.map { exprs -> exprs.map(::Sum) }

    override fun copyForImpl(context: Context): ExprEditor<Sum> = SumEditor(context, expressions.copyFor(context))

    override fun components(): Sequence<Any> = sequenceOf(expressions)
}