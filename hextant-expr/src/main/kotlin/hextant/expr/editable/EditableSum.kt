/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.expr.editable

import hextant.RResult
import hextant.base.AbstractEditable
import hextant.expr.edited.Sum
import hextant.map
import kserial.*
import reaktive.value.binding.map

class EditableSum(val expressions: EditableExprList = EditableExprList()) : AbstractEditable<Sum>(),
                                                                            EditableExpr<Sum> {
    override val result: RResult<Sum> = expressions.result.map { exprs -> exprs.map(::Sum) }

    companion object : Serializer<EditableSum> {
        override fun deserialize(cls: Class<EditableSum>, input: Input, context: SerialContext): EditableSum {
            val expressions = input.readTyped<EditableExprList>(context)!!
            return EditableSum(expressions)
        }

        override fun serialize(obj: EditableSum, output: Output, context: SerialContext) {
            output.writeObject(obj.expressions, context)
        }
    }
}