package hextant.config

import hextant.Context
import hextant.command.Commands
import hextant.command.line.CommandCompleter
import hextant.completion.CompoundCompleter

/**
 * Completer for [Enabled]-objects.
 */
class EnabledCompleter(context: Context) : CompoundCompleter<Context, Enabled>() {
    init {
        addCompleter(CommandCompleter { context[Commands].all() })
    }
}