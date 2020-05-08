/**
 * @author Nikolaus Knop
 */

package hextant.bundle

import reaktive.event.EventStream
import reaktive.value.ReactiveValue

/**
 * A Bundle is used to get and set properties
 */
interface Bundle {
    /**
     * This stream emits events when the value of property changes
     */
    val changed: EventStream<BundleChange<*>>

    /**
     * Return `true` if the given [property] is configured in this [Bundle].
     * You need to pass the [Read] permission
     */
    fun <Read : Any> hasProperty(permission: Read, property: Property<*, Read, *>): Boolean

    /**
     * Return `true` if the given [property] is configured in this [Bundle].
     */
    fun hasProperty(property: Property<*, Any, *>): Boolean

    /**
     * Get the value of the specified [property] in this [Bundle], or the [Property.default] value, if not present.
     * You need to pass the [Read] permission.
     * @throws NoSuchElementException if the specified [property] was not set before and no default value is provided.
     */
    operator fun <Read : Any, T> get(permission: Read, property: Property<out T, Read, *>): T

    /**
     * Return a [ReactiveValue] always holding the value of the specified [property] in this [Bundle], or the [Property.default] value, if not present.
     * You need to pass the [Read] permission.
     * @throws NoSuchElementException if the specified [property] was not set before and no default value is provided.
     */
    operator fun <Read : Any, T> get(
        permission: Read,
        property: ReactiveProperty<out T, Read, *>
    ): ReactiveValue<T>

    /**
     * Get the value of the specified [property] in this [Bundle], or the [Property.default] value, if not present.
     * @throws NoSuchElementException if the specified [property] was not set before and no default value is provided.
     */
    operator fun <T> get(property: Property<out T, Any, *>): T

    /**
     * Return a [ReactiveValue] always holding the value of the specified [property] in this [Bundle], or the [Property.default] value, if not present.
     * @throws NoSuchElementException if the specified [property] was not set before and no default value is provided.
     */
    operator fun <T> get(property: ReactiveProperty<out T, Any, *>): ReactiveValue<T>

    /**
     * Set the value of the specified [property] to the constant [value] in this [Bundle].
     * Calls of `get(permission, property)` will return this [value] until a new value is set
     * You need to pass the [Write] permission
     */
    operator fun <Write : Any, T> set(write: Write, property: Property<in T, *, Write>, value: T)

    /**
     * Set the value of the specified [property] to the constant [value] in this [Bundle].
     * Calls of `get(permission, property)` will return this [value] until a new value is set.
     */
    operator fun <T> set(property: Property<in T, *, Any>, value: T)

    /**
     * Deletes the value of the given [property] from this bundle.
     * @throws NoSuchElementException if the given [property] is not associated with any value
     */
    fun <Write : Any> delete(property: Property<*, Write, *>)
}