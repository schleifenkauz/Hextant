/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.context.Context
import hextant.core.editor.CompoundEditor
import hextant.expr.Sum
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

@ProvideFeature
class SumEditor(
    context: Context
) : CompoundEditor<Sum>(context), ExprEditor<Sum> {
    val expressions by child(ExprListEditor(context))

    init {
        expressions.ensureNotEmpty()
    }

    override val result: ReactiveValidated<Sum> = composeReactive(expressions.result, ::Sum)
}