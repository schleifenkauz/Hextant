/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")

package org.nikok.hextant.bundle

import org.nikok.hextant.bundle.Bundle.Value.*
import kotlin.properties.ReadOnlyProperty

/**
 * A Property Holder is used to get and set properties
 */
interface Bundle {
    /**
     * Get the value of the specified [property] in this [Bundle].
     * You need to pass the [Read] [Permission]
     * @throws NoSuchPropertyException if the specified [property] was not set before
     */
    operator fun <T : Any, Read : Permission> get(
        permission: Read,
        property: Property<out T, Read, *>
    ): T

    /**
     * Set the value of the specified [property] to the constant [value] in this [Bundle].
     * Calls of `get(permission, property)` will return this [value] until a new value is set
     * You need to pass the [Write] [Permission]
     */
    operator fun <T : Any, Write : Permission> set(
        permission: Write, property: Property<in T, *, Write>,
        value: T
    )

    /**
     * Set a factory for the specified [property] in this [Bundle].
     * Calls of `get(permission, property) will call [factory] and return the result until a new value is set`
     */
    fun <T : Any, Write : Permission> setFactory(
        permission: Write,
        property: Property<in T, *, Write>,
        factory: () -> T
    )

    /**
     * Set a delegate for the specified [property] in this [Bundle].
     * Calls of `get(permission, property) will delegate to the specified [delegate]`
     */
    fun <T : Any, Write : Permission> setBy(
        permission: Write,
        property: Property<in T, *, Write>,
        delegate: ReadOnlyProperty<Nothing?, T>
    )

    /**
     * @return a new [Bundle] with the same properties as this property holder
     * and apply the [init] block to the new property holder.
     */
    fun copy(init: Init.() -> Unit): Bundle

    private sealed class Value<out T> {
        abstract val value: T

        data class Constant<T>(override val value: T) : Value<T>()
        data class Factory<T>(private val factory: () -> T) : Value<T>() {
            override val value: T
                get() = factory()
        }

        data class Delegate<T : Any?>(val delegate: ReadOnlyProperty<Nothing?, T>) : Value<T>() {
            override val value: T get() = delegate.getValue(null, this::value)
        }
    }

    private class Impl(private val properties: MutableMap<Property<*, *, *>, Value<Any>> = mutableMapOf())
        : Bundle {
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
            return properties[property]?.value as T?
                   ?: throw NoSuchPropertyException("Property $property not configured")
        }

        override fun copy(init: Init.() -> Unit): Bundle = newInstance {
            this as InitImpl
            for ((p, v) in properties) {
                setValue(p as Property<Any, *, *>, v)
            }
            init()
        }
    }

    companion object {
        /**
         * @return a new [Bundle]
         */
        fun newInstance(): Bundle = Impl()

        /**
         * @return a new [Bundle] first applying the init block to it
         */
        fun newInstance(init: Bundle.Init.() -> Unit): Bundle = InitImpl().apply(init).build()
    }

    /**
     * Used to initialize [Bundle]s, the setter methods work the same,
     * just that you don't need any permissions to use them
     */
    interface Init {
        /**
         * Works the same as [Bundle.set]
        */
        fun <T : Any> set(property: Property<in T, *, *>, value: T)

        /**
         * Works the same as [Bundle.setFactory]
        */
        fun <T : Any> setFactory(property: Property<in T, *, *>, factory: () -> T)

        /**
         * Works the same as [Bundle.setBy]
        */
        fun <T : Any> setBy(property: Property<in T, *, *>, delegate: ReadOnlyProperty<Nothing?, T>)
    }

    private class InitImpl : Init {
        private val properties = mutableMapOf<Property<*, *, *>, Value<Any>>()

        override fun <T : Any> set(property: Property<in T, *, *>, value: T) {
            setValue(property, Constant(value))
        }

        fun <T : Any> setValue(
            property: Property<in T, *, *>,
            value: Value<T>
        ) {
            properties[property] = value
        }

        override fun <T : Any> setFactory(
            property: Property<in T, *, *>,
            factory: () -> T
        ) {
            setValue(property, Factory(factory))
        }

        override fun <T : Any> setBy(
            property: Property<in T, *, *>,
            delegate: ReadOnlyProperty<Nothing?, T>
        ) {
            setValue(property, Delegate(delegate))
        }

        fun build(): Bundle {
            return Impl(properties)
        }
    }
}