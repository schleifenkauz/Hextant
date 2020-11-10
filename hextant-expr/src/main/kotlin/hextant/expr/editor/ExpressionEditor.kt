package hextant.expr.editor

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.expr.Expression

class ExpressionEditor @ProvideProjectType("Expression") constructor(context: Context) :
    CompoundEditor<Expression>(context) {
    val root by child(ExprExpander(context))

    override val result = composeResult { Expression(root.get()) }
}