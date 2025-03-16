package hextant.expr.editor

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.expr.Expression
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.ReactiveValue

@Serializable
class ExpressionEditor @ProvideProjectType("Expression") constructor() : CompoundEditor<@Contextual Expression?>() {
    val root by child(ExprExpander())

    init {
        initialize(context)
    }

    @Transient
    final override lateinit var result: ReactiveValue<Expression?>
        private set

    override fun doInitialize() {
        result = composeResult { Expression(root.get()) }
    }
}