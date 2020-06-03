/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.Context
import hextant.base.CompoundEditor
import hextant.expr.Sum
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class SumEditor(
    context: Context
) : CompoundEditor<Sum>(context), ExprEditor<Sum> {
    val expressions by child(ExprListEditor(context))

    override val result: ReactiveValidated<Sum> = composeReactive(expressions.result, ::Sum)
}