package hextant.expr.editor

import hextant.codegen.ProvideProjectType
import hextant.core.editor.CompoundEditor
import hextant.expr.Expression
import kotlinx.serialization.Contextual
import kotlinx.serialization.Transient
import reaktive.value.ReactiveValue

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