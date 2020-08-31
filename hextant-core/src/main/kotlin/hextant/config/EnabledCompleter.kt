package hextant.config

import hextant.completion.Completion
import hextant.completion.CompoundCompleter
import hextant.context.Context
import reaktive.value.now

internal class EnabledCompleter(private val enabled: Boolean) : CompoundCompleter<Context, Enabled>() {
    override fun completions(context: Context, input: String): Collection<Completion<Enabled>> =
        Companion.completions(context, input).filter { it.completion.isEnabled.now == enabled }

    companion object : CompoundCompleter<Context, Enabled>() {
        init {
            addCompleter(CommandCompleter)
            addCompleter(InspectionCompleter)
        }
    }
}