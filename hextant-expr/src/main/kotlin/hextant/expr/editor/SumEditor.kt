/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.expr.Sum
import reaktive.value.ReactiveValue

@ProvideFeature
class SumEditor : CompoundEditor<Sum?>(), ExprEditor<Sum> {
    val expressions by child(ExprListEditor())

    init {
        expressions.ensureNotEmpty()
    }

    override val result: ReactiveValue<Sum?> = composeResult { Sum(expressions.now) }
}