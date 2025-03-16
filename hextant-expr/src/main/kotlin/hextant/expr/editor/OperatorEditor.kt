package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.EditorFactory
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.expr.Operator
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@ProvideFeature
@Serializable
class OperatorEditor @ProvideImplementation(EditorFactory::class) constructor() :
    TokenEditor<@Contextual Operator?, TokenEditorView>() {
    constructor(operator: Operator) : this() {
        setInitialText(operator.name)
    }

    override fun compile(token: String): Operator? = token.takeIf { Operator.isValid(it) }?.let { Operator.of(it) }
}
