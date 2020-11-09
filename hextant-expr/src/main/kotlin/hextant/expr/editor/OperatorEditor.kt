package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.Operator
import validated.*

@ProvideFeature
class OperatorEditor constructor(context: Context, text: String) :
    TokenEditor<Operator, TokenEditorView>(context, text) {
    constructor(context: Context, operator: Operator) : this(context, operator.name)

    @ProvideImplementation(EditorFactory::class)
    constructor(context: Context) : this(context, "")

    override fun wrap(token: String): Operator =
        token.takeIf { Operator.isValid(it) }.validated { invalid("Invalid operator $token") }.map { Operator.of(it) }
}
