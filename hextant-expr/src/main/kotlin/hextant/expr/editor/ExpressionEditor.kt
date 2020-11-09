package hextant.expr.editor

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.core.editor.composeResult
import hextant.expr.Expression
import reaktive.value.ReactiveValue

class ExpressionEditor @ProvideProjectType("Expression") constructor(context: Context) :
    CompoundEditor<Expression>(context) {
    val root by child(ExprExpander(context))

    override val result: ReactiveValue<Expression?> = composeResult(root)
}