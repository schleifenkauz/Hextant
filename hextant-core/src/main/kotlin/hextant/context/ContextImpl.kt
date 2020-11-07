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
open class ContextImpl(final override val parent: Context?, private val bundle: Bundle = createBundle()) : Context {

    override fun <T : Any> get(property: Property<out T, *>): T =
        when {
            bundle.hasProperty(property) -> bundle[property]
            parent != null               -> parent[property]
            property.default != null     -> property.default!!
            else                         -> throw NoSuchElementException("Property $property is not configured")
        }

    override fun <P : Permission> delete(permission: P, property: Property<*, P>) {
        when {
            bundle.hasProperty(property) -> bundle.delete(permission, property)
            parent != null               -> parent.delete(permission, property)
            else                         -> throw NoSuchElementException("Property $property is not configured")
        }
    }

    override fun hasProperty(property: Property<*, *>): Boolean =
        bundle.hasProperty(property) || parent?.hasProperty(property) ?: false

    override val changed: EventStream<BundleChange<*>> get() = bundle.changed

    override fun <T : Any> getReactive(property: Property<out T, *>): ReactiveValue<T> =
        when {
            bundle.hasProperty(property) -> bundle.getReactive(property)
            parent != null               -> parent.getReactive(property)
            else                         -> bundle.getReactive(property)
        }

    override fun <P : Permission, T : Any> set(permission: P, property: Property<in T, P>, value: T) {
        bundle[permission, property] = value
    }

    override val entries: Sequence<BundleEntry<*>> get() = bundle.entries + parent?.entries.orEmpty()
}