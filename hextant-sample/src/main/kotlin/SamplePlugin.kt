import hextant.expr.editor.SumEditor
import hextant.plugin.PluginInitializer
import hextant.plugin.registerInspection
import reaktive.value.binding.equalTo
import validated.reaktive.mapValidated
import validated.valid

object SamplePlugin : PluginInitializer({
    registerInspection<SumEditor> {
        id = "sum-zero"
        description = "Prevents sums that evaluate to zero"
        isSevere(false)
        message { "Sum is zero" }
        preventingThat {
            inspected.expressions.result.mapValidated { it.sumBy { e -> e.value } }.equalTo(valid(0))
        }
    }
})