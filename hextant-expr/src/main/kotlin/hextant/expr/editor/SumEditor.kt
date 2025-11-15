/**
 *@author Nikolaus Knop
 */

package hextant.expr.editor

import hextant.codegen.ProvideFeature
import hextant.core.editor.CompoundEditor
import hextant.core.editor.defaultState
import hextant.expr.Sum
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import reaktive.value.ReactiveValue

@ProvideFeature
class SumEditor : CompoundEditor<@Contextual Sum?>(), ExprEditor<Sum> {
    @Transient
    override lateinit var result: ReactiveValue<Sum?>
        private set

    val expressions by child(ExprListEditor().defaultState())

    override fun doInitialize() {
        super.doInitialize()
        result = composeResult { Sum(expressions.now) }
        expressions.ensureNotEmpty()
    }
}