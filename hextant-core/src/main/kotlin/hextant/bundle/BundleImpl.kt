/**
 *@author Nikolaus Knop
 */

package hextant.bundle

import reaktive.event.EventStream
import reaktive.event.event
import reaktive.impl.WeakReactive
import reaktive.value.*

internal class BundleImpl : Bundle {
    private val change = event<BundleChange<*>>()
    override val changed: EventStream<BundleChange<*>>
        get() = change.stream
    private val properties = mutableMapOf<Property<*, *, *>, Any>()
    private val managed = mutableMapOf<ReactiveProperty<*, *, *>, WeakReactive<ReactiveVariable<Any>>>()

    override fun <Read : Any> hasProperty(permission: Read, property: Property<*, Read, *>): Boolean =
        property in properties

    override fun hasProperty(property: Property<*, Any, *>): Boolean = hasProperty(Any(), property)

    @Suppress("UNCHECKED_CAST")
    override fun <Read : Any, T : Any> get(permission: Read, property: Property<out T, Read, *>): T {
        val value = properties[property] as T?
        return value ?: property.default() ?: throw NoSuchElementException("No value for property $property")
    }

    override fun <Read : Any, T : Any> get(
        permission: Read,
        property: ReactiveProperty<out T, Read, *>
    ): ReactiveValue<T> {
        val value = get(permission, property as Property<out T, Read, *>)
        return getReactive(property, value)
    }

    override fun <T : Any> get(property: Property<out T, Any, *>): T = get(Any(), property)

    override fun <T : Any> get(property: ReactiveProperty<out T, Any, *>): ReactiveValue<T> {
        val value = get(property as Property<out T, Any, *>)
        return getReactive(property, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Write : Any, T : Any> set(write: Write, property: Property<in T, *, Write>, value: T) {
        val old = properties[property]
        properties[property] = value
        if (property is ReactiveProperty) {
            change.fire(BundleChange(this, property, old as T?, value))
            managed[property]?.reactive?.set(value)
        }
    }

    override fun <T : Any> set(property: Property<in T, *, Any>, value: T) {
        set(Any(), property, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Read : Any, T : Any> getReactive(
        property: ReactiveProperty<out T, Read, *>,
        value: T
    ): ReactiveValue<T> {
        val reactive = managed[property]?.reactive
        return if (reactive == null) {
            val new = reactiveVariable(value)
            managed[property] = new.weak as WeakReactive<ReactiveVariable<Any>>
            new
        } else reactive as ReactiveValue<T>
    }

    override fun <Write : Any> delete(property: Property<*, Write, *>) {
        properties.remove(property) ?: throw NoSuchElementException("Cannot delete $property")
        if (property is ReactiveProperty) {
            val v = managed[property]?.reactive
            if (v != null) {
                val default = property.default()
                if (default != null) v.set(default)
                else throw NoSuchElementException("Value of reactive property was deleted")
            }
        }
    }
}