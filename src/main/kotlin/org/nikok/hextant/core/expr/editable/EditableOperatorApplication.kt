/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import kserial.*
import org.nikok.hextant.Editable
import org.nikok.hextant.core.expr.edited.Operator
import org.nikok.hextant.core.expr.edited.OperatorApplication
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.value.*
import org.nikok.reaktive.value.binding.binding

@SerializableWith(EditableOperatorApplication.Serial::class)
class EditableOperatorApplication(
    override val parent: Editable<*>? = null,
    val editableOperator: EditableOperator = EditableOperator(),
    val editableOp1: ExpandableExpr = ExpandableExpr(),
    val editableOp2: ExpandableExpr = ExpandableExpr()
) : Editable<OperatorApplication> {
    constructor(operator: Operator, parent: Editable<*>? = null) : this(parent) {
        editableOperator.editableText.text.set(operator.name)
    }

    override val children: Collection<Editable<*>>?
        get() = listOf(editableOp1, editableOperator, editableOp2)

    override val edited: ReactiveValue<OperatorApplication?> =
        binding<OperatorApplication?>(
            "operator application",
            dependencies(editableOp1.edited, editableOp2.edited, editableOperator.edited)
        ) {
            val operator = editableOperator.edited.now ?: return@binding null
            val op1 = editableOp1.edited.now ?: return@binding null
            val op2 = editableOp2.edited.now ?: return@binding null
            OperatorApplication(op1, op2, operator)
        }

    override val isOk: ReactiveBoolean = edited.map("is $this ok") { it != null }

    object Serial : Serializer<EditableOperatorApplication> {
        override fun serialize(obj: EditableOperatorApplication, output: Output, context: SerialContext) {
            with(output) {
                writeObject(obj.editableOperator, context)
                writeObject(obj.editableOp1, context)
                writeObject(obj.editableOp2, context)
            }
        }

        override fun deserialize(
            cls: Class<EditableOperatorApplication>,
            input: Input,
            context: SerialContext
        ): EditableOperatorApplication = with(input) {
            EditableOperatorApplication(
                null,
                readTyped(context)!!,
                readTyped(context)!!,
                readTyped(context)!!
            )
        }
    }
}