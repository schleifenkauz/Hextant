package hextant.sample.editor

import hextant.codegen.ProvideFeature
import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.editor.CompoundEditor
import hextant.sample.ForLoop
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

@ProvideFeature
class ForLoopEditor @ProvideImplementation(EditorFactory::class) constructor(context: Context) :
    CompoundEditor<ForLoop>(context), StatementEditor<ForLoop> {
    private val initializerContext = context.child()

    val initializer by child(StatementExpander(initializerContext))
    val condition by child(ExprExpander(initializerContext))
    val after by child(StatementExpander(initializerContext))
    val body by child(BlockEditor(context))
    override val result: ReactiveValidated<ForLoop> =
        composeReactive(initializer.result, condition.result, after.result, body.result, ::ForLoop)
}
