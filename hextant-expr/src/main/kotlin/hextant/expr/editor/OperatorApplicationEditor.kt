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
    val operatorEditor: OperatorEditor,
    val editableOp1: ExprExpander,
    val editableOp2: ExprExpander,
    context: Context
) : AbstractEditor<OperatorApplication, Any>(context),
    ExprEditor<OperatorApplication> {
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
        OperatorEditor(edited.operator, context),
        ExprExpander(edited.op1, context),
        ExprExpander(edited.op2, context),
        context
    )

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