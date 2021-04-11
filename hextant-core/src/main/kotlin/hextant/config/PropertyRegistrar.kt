/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.PublicProperty
import bundles.property

internal class PropertyRegistrar {
    val configurable = mutableSetOf<ConfigurableProperty<*>>()

    companion object : PublicProperty<PropertyRegistrar> by property("property registrar")
}