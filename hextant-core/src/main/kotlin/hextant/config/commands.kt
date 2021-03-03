/**
 * @author Nikolaus Knop
 */

package hextant.config

import hextant.command.command
import hextant.context.Context

internal val enable = command<Context, String> {
    name = "enable"
    description = "Enables the given object"
    shortName = "enable"
    val enabled = addParameter<Feature> {
        editWith { ctx -> FeatureIdEditor(ctx, enabled = false) }
        name = "enabled"
        description = "The object to enable"
    }
    executing { ctx, args ->
        val feature = args[enabled]
        val registrar = ctx[FeatureRegistrar]
        if (registrar.isEnabled(feature)) "Already enabled"
        else {
            registrar.enable(feature)
            "Enabled ${feature.id}"
        }
    }
}

internal val disable = command<Context, String> {
    name = "disable"
    description = "Disables the given object"
    shortName = "disable"
    val enabled = addParameter<Feature> {
        editWith { ctx -> FeatureIdEditor(ctx, enabled = true) }
        name = "disabled"
        description = "The object to disabled"
    }
    executing { ctx, args ->
        val feature = args[enabled]
        val registrar = ctx[FeatureRegistrar]
        if (!registrar.isEnabled(feature)) "Already disabled"
        else {
            registrar.disable(feature)
            "Disabled ${feature.id}"
        }
    }
}

