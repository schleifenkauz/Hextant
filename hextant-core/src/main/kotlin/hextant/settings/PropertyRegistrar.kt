/**
 *@author Nikolaus Knop
 */

package hextant.settings

import bundles.SimpleProperty

internal class PropertyRegistrar {
    val configurable = mutableSetOf<ConfigurableProperty<*>>()

    companion object : SimpleProperty<PropertyRegistrar>("property registrar")
}