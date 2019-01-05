/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.core.expr.editable

import hextant.base.AbstractEditable
import hextant.core.expr.edited.Expr
import hextant.core.expr.edited.Sum
import kserial.*
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.now

class EditableSum(val expressions: EditableExprList = EditableExprList()) : AbstractEditable<Sum>(), EditableExpr<Sum> {
    override val edited: ReactiveValue<Sum?>
        get() = expressions.edited.map("edited of sum") { editableExpressions ->
            editableExpressions.map { editable ->
                editable?.edited?.now
            }.takeIf { expressions ->
                expressions.all { expr -> expr != null }
            }?.let { Sum(it as List<Expr>) }
        }

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