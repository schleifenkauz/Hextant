package hextant.config

import hextant.Context
import hextant.completion.CompoundCompleter

/**
 * Completer for [Enabled]-objects.
 */
object EnabledCompleter : CompoundCompleter<Context, Enabled>() {
    init {
        addCompleter(CommandCompleter)
        addCompleter(InspectionCompleter)
    }
}