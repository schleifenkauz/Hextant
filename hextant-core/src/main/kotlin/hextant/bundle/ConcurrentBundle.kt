/**
 *@author Nikolaus Knop
 */

package hextant.bundle

import kotlin.properties.ReadOnlyProperty

/**
 * A [Bundle] synchronizing the mutating methods
 */
class ConcurrentBundle(private val bundle: Bundle) : Bundle by bundle {
    @Synchronized override fun <T : Any, Write : Permission> set(
        permission: Write,
        property: Property<in T, *, Write>,
        value: T
    ) {
        bundle[permission, property] = value
    }

    @Synchronized override fun <T : Any, Write : Permission> setFactory(
        permission: Write,
        property: Property<in T, *, Write>,
        factory: () -> T
    ) {
        bundle.setFactory(permission, property, factory)
    }

    @Synchronized override fun <T : Any, Write : Permission> setBy(
        permission: Write,
        property: Property<in T, *, Write>,
        delegate: ReadOnlyProperty<Nothing?, T>
    ) {
        bundle.setBy(permission, property, delegate)
    }
}