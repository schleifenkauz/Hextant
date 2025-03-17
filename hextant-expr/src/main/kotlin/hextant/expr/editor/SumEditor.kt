/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.expr.Sum
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.ReactiveValue

@ProvideFeature
class SumEditor : CompoundEditor<@Contextual Sum?>(), ExprEditor<Sum> {
    val expressions by child(ExprListEditor())

    init {
        expressions.ensureNotEmpty()
    }

    @Transient
    override lateinit var result: ReactiveValue<Sum?>
        private set


    override fun doInitialize() {
        result = composeResult { Sum(expressions.now) }
    }
}