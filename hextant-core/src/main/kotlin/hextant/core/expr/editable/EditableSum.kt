/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.core.expr.editable

import hextant.base.AbstractEditable
import hextant.core.expr.edited.Sum
import kserial.*
import reaktive.value.ReactiveValue
import reaktive.value.binding.map

class EditableSum(val expressions: EditableExprList = EditableExprList()) : AbstractEditable<Sum>(), EditableExpr<Sum> {
    override val edited: ReactiveValue<Sum?>
        get() = expressions.edited.map { editableExpressions -> editableExpressions?.let { Sum(it) } }

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