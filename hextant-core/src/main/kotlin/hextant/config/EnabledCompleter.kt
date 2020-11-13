package hextant.config

import hextant.completion.Completion
import hextant.completion.CompoundCompleter
import hextant.context.Context
import hextant.core.editor.TokenEditor
import reaktive.value.now

internal class EnabledCompleter(private val enabled: Boolean) : CompoundCompleter<TokenEditor<*, *>, Enabled>() {
    override fun completions(context: TokenEditor<*, *>, input: String): Collection<Completion<Enabled>> =
        Companion.completions(context.context, input).filter { it.item.isEnabled.now == enabled }

    companion object : CompoundCompleter<Context, Enabled>() {
        init {
            addCompleter(CommandCompleter)
            addCompleter(InspectionCompleter)
        }
    }
}