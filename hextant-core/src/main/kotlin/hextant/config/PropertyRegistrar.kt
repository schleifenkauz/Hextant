/**
 *@author Nikolaus Knop
 */

package hextant.config

import bundles.PublicProperty
import bundles.publicProperty

internal class PropertyRegistrar {
    val configurable = mutableSetOf<ConfigurableProperty<*>>()

    companion object : PublicProperty<PropertyRegistrar> by publicProperty("property registrar")
}