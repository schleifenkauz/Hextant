/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.expr.edited.Operator
import hextant.expr.edited.OperatorApplication
import reaktive.dependencies
import reaktive.value.binding.binding
import reaktive.value.now

class OperatorApplicationEditor(
    operatorEditor: OperatorEditor,
    editableOp1: ExprExpander,
    editableOp2: ExprExpander,
    context: Context
) : AbstractEditor<OperatorApplication, Any>(context), ExprEditor<OperatorApplication> {
    val operatorEditor = operatorEditor.moveTo(context)
    val editableOp1 = editableOp1.moveTo(context)
    val editableOp2 = editableOp2.moveTo(context)

    constructor(operator: Operator, context: Context) : this(context) {
        operatorEditor.setText(operator.name)
    }

    init {
        children(editableOp1, operatorEditor, editableOp2)
    }

    constructor(context: Context) : this(
        OperatorEditor(context),
        ExprExpander(context),
        ExprExpander(context),
        context
    )

    constructor(context: Context, edited: OperatorApplication) : this(
        OperatorEditor(context, edited.operator),
        ExprExpander(edited.op1, context),
        ExprExpander(edited.op2, context),
        context
    )

    override fun copyForImpl(context: Context): OperatorApplicationEditor {
        return OperatorApplicationEditor(
            operatorEditor.copyFor(context),
            editableOp1.copyFor(context),
            editableOp2.copyFor(context),
            context
        )
    }

    override val result: EditorResult<OperatorApplication> =
        binding<CompileResult<OperatorApplication>>(
            dependencies(editableOp1.result, editableOp2.result, operatorEditor.result)
        ) {
            val operator = operatorEditor.result.now.ifErr { return@binding ChildErr }
            val op1 = editableOp1.result.now.ifErr { return@binding ChildErr }
            val op2 = editableOp2.result.now.ifErr { return@binding ChildErr }
            Ok(OperatorApplication(op1, op2, operator))
        }
}