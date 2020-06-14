package hextant.config

import hextant.Context
import hextant.command.Command
import hextant.command.Commands
import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter

internal object CommandCompleter : ConfiguredCompleter<Context, Command<*, *>>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<Command<*, *>> = context[Commands].all()

    override fun extractText(context: Context, item: Command<*, *>): String? = item.id

    override fun Builder<Command<*, *>>.configure(context: Context) {
        infoText = "command"
    }
}