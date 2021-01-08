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
    val enabled = addParameter<Enabled> {
        editWithValidated { ctx -> EnabledEditor(ctx, enabled = false) }
        name = "enabled"
        description = "The object to enable"
    }
    executing { _, args ->
        val e = args[enabled]
        if (e.isEnabled.now) "Already enabled"
        else {
            e.enable()
            "Enabled ${e.id}"
        }
    }
}

internal val disable = command<Context, String> {
    name = "disable"
    description = "Disables the given object"
    shortName = "disable"
    val enabled = addParameter<Enabled> {
        editWithValidated { ctx -> EnabledEditor(ctx, enabled = true) }
        name = "disabled"
        description = "The object to disabled"
    }
    executing { _, args ->
        val e = args[enabled]
        if (!e.isEnabled.now) "Already disabled"
        else {
            e.disable()
            "Disabled ${e.id}"
        }
    }
}

