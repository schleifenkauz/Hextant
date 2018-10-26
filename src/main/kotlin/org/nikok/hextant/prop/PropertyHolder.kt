/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")

package org.nikok.hextant.prop

import org.nikok.hextant.prop.PropertyHolder.Impl.Value.*
import kotlin.properties.ReadOnlyProperty

interface PropertyHolder {
    operator fun <T : Any, Read : Permission> get(
        permission: Read,
        property: Property<out T, Read, *>
    ): T

    fun <T : Any, Write : Permission> set(
        permission: Write, property: Property<in T, *, Write>,
        value: T
    )

    fun <T : Any, Write : Permission> setFactory(
        permission: Write,
        property: Property<in T, *, Write>,
        factory: () -> T
    )

    fun <T: Any, Write: Permission> setBy(
        permission: Write,
        property: Property<in T, *, Write>,
        delegate: ReadOnlyProperty<Nothing?, T>
    )

    private class Impl: PropertyHolder {
        private sealed class Value<out T> {
            abstract val value: T

            data class Constant<T>(override val value: T) : Value<T>()
            data class Factory<T>(private val factory: () -> T): Value<T>() {
                override val value: T
                    get() = factory()
            }
            data class Delegate<T : Any?>(val delegate: ReadOnlyProperty<Nothing?, T>): Value<T>() {
                override val value: T get() = delegate.getValue(null, this::value)
            }
        }

        private val properties = mutableMapOf<Property<*, *, *>, Value<Any>>()

        override fun <T : Any, Write : Permission> set(
            permission: Write,
            property: Property<in T, *, Write>,
            value: T
        ) {
            properties[property] = Constant(value)
        }

        override fun <T : Any, Write : Permission> setFactory(
            permission: Write,
            property: Property<in T, *, Write>,
            factory: () -> T
        ) {
            properties[property] = Factory(factory)
        }

        override fun <T : Any, Write : Permission> setBy(
            permission: Write,
            property: Property<in T, *, Write>,
            delegate: ReadOnlyProperty<Nothing?, T>
        ) {
            properties[property] = Delegate(delegate)
        }

        override fun <T : Any, Read : Permission> get(permission: Read, property: Property<out T, Read, *>): T {
            return properties[property]?.value as T? ?: throw NoSuchElementException("Property $property not configured")
        }
    }

    companion object {
        fun newInstance(): PropertyHolder = Impl()
    }
}