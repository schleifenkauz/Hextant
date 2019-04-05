/**
 *@author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")

package hextant.bundle

import hextant.bundle.Bundle.Value.*
import kotlin.properties.ReadOnlyProperty

/**
 * A Bundle is used to get and set properties
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

    private class Impl(private val properties: MutableMap<Property<*, *, *>, Value<Any>> = mutableMapOf()) : Bundle {
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
                ?: throw NoSuchPropertyException("$property not configured")
        }
    }

    companion object {
        /**
         * @return a new [Bundle]
         */
        fun newInstance(): Bundle = Impl()

        /**
         * @return a new [Bundle], first applying [init] to it
        */
        inline fun configure(init: Bundle.() -> Unit) = newInstance().apply(init)

        /**
         * @return a concurrent [Bundle] wrapping the passed [bundle]
         */
        fun concurrent(bundle: Bundle = newInstance()) = ConcurrentBundle(bundle)

        /**
         * @return a reactive [Bundle] wrapping the passed [bundle]
         */
        fun reactive(bundle: Bundle = newInstance()) = ReactiveBundle(bundle)
    }
}