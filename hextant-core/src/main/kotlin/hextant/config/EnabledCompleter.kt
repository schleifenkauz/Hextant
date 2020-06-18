package hextant.config

import hextant.completion.CompoundCompleter
import hextant.context.Context

/**
 * Completer for [Enabled]-objects.
 */
object EnabledCompleter : CompoundCompleter<Context, Enabled>() {
    init {
        addCompleter(CommandCompleter)
        addCompleter(InspectionCompleter)
    }
}