/**
 * @author Nikolaus Knop
 */

package hextant.config

import hextant.command.command
import hextant.context.Context
import reaktive.value.now

internal val enable = command<Context, String> {
    name = "enable"
    description = "Enables the given object"
    shortName = "enable"
    addParameter<Enabled> {
        name = "enabled"
        description = "The object to enable"
    }
    executing { _, (enabled) ->
        enabled as Enabled
        if (enabled.isEnabled.now) "Already enabled"
        else {
            enabled.enable()
            "Enabled ${enabled.id}"
        }
    }
}

internal val disable = command<Context, String> {
    name = "disable"
    description = "Disables the given object"
    shortName = "disable"
    addParameter<Enabled> {
        name = "disabled"
        description = "The object to disabled"
    }
    executing { _, (enabled) ->
        enabled as Enabled
        if (!enabled.isEnabled.now) "Already disabled"
        else {
            enabled.disable()
            "Disabled ${enabled.id}"
        }
    }
}

