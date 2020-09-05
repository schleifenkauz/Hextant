/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.*
import reaktive.event.EventStream
import reaktive.value.ReactiveValue

/**
 * Skeletal implementation of [Context] using the specified [parent] and [bundle]
 */
open class AbstractContext(final override val parent: Context?, private val bundle: Bundle = createBundle()) : Context {

    override fun <Read : Any, T> get(permission: Read, property: Property<out T, Read, *>): T =
        when {
            bundle.hasProperty(permission, property) -> bundle[permission, property]
            parent != null                           -> parent[permission, property]
            else                                     -> property.default
        }

    override fun <Read : Any, Write : Read> delete(permission: Write, property: Property<*, Read, Write>) {
        when {
            bundle.hasProperty(permission, property) -> bundle.delete(permission, property)
            parent != null                           -> parent.delete(permission, property)
            else                                     -> throw NoSuchElementException("Property $property is not configured")
        }
    }

    override fun <T> get(property: Property<out T, Any, *>): T = get(Any(), property)

    override fun <Read : Any> hasProperty(permission: Read, property: Property<*, Read, *>): Boolean =
        bundle.hasProperty(permission, property) || parent?.hasProperty(permission, property) ?: false

    override fun hasProperty(property: Property<*, Any, *>): Boolean = hasProperty(Any(), property)

    override val changed: EventStream<BundleChange<*>> get() = bundle.changed

    override fun delete(property: Property<*, Any, Any>) {
        delete(Any(), property)
    }

    override fun <Read : Any, T> get(permission: Read, property: ReactiveProperty<out T, Read, *>): ReactiveValue<T> =
        when {
            bundle.hasProperty(permission, property) -> bundle[permission, property]
            parent != null                           -> parent[permission, property]
            else                                     -> bundle[permission, property]
        }

    override fun <T> get(property: ReactiveProperty<out T, Any, *>): ReactiveValue<T> = get(Any(), property)

    override fun <Write : Any, T> set(write: Write, property: Property<in T, *, Write>, value: T) {
        bundle[write, property] = value
    }

    override fun <T> set(property: Property<in T, *, Any>, value: T) {
        set(Any(), property, value)
    }

    override val entries: Sequence<Pair<Property<*, *, *>, Any?>> get() = bundle.entries
}