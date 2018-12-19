/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.bundle

import kotlin.properties.ReadOnlyProperty

/**
 * A context in which you have Permission [P]
*/
class PermissionContext<out P: Permission> internal constructor(private val permission: P) {
    /**
     * Delegates to the [Bundle]
    */
    operator fun <T : Any> Bundle.get(property: Property<out T, P, *>) = get(permission, property)

    /**
     * Delegates to the [Bundle]
    */
    operator fun <T : Any> Bundle.set(property: Property<in T, *, P>, value: T) {
        set(permission, property, value)
    }

    /**
     * Delegates to the [Bundle]
    */
    fun <T : Any> Bundle.setBy(property: Property<T, *, P>, delegate: ReadOnlyProperty<Nothing?, T>) {
        setBy(permission, property, delegate)
    }

    /**
     * Delegates to the [Bundle]
    */
    fun <T : Any> Bundle.setFactory(property: Property<T, *, P>, factory: () -> T) {
        setFactory(permission, property, factory)
    }
}

/**
 * Execute [block] with permission [P]
*/
operator fun <P: Permission, R> P.invoke(block: PermissionContext<P>.() -> R): R = PermissionContext(this).block()