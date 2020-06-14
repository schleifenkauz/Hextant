package hextant.config

import hextant.Context
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

internal object EnabledCompleter : ConfiguredCompleter<Context, Enabled>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<Enabled> = context[EnabledSource].all()

    override fun extractText(context: Context, item: Enabled): String? = item.name
}