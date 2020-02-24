/**
 *@author Nikolaus Knop
 */

package hextant.settings.model

import hextant.bundle.Internal
import hextant.bundle.Property

class ConfigurableProperties {
    private val properties = mutableMapOf<String, ConfigurableProperty>()

    fun register(property: ConfigurableProperty) {
        val name = property.property.name ?: error("Configurable properties must have a name")
        properties[name] = property
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> register(property: Property<T, *, *>) {
        register(
            ConfigurableProperty(
                property as Property<Any, Any, Any>,
                T::class
            )
        )
    }

    fun byName(name: String) = properties[name]

    fun all(): Collection<ConfigurableProperty> = properties.values

    companion object : Property<ConfigurableProperties, Any, Internal>("configurable properties")
}