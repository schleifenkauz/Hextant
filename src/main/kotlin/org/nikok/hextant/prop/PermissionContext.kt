/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.prop

import kotlin.properties.ReadOnlyProperty

class PermissionContext<out P: Permission> internal constructor(private val permission: P) {
    operator fun <T : Any> PropertyHolder.get(property: Property<out T, P, *>) = get(permission, property)

    operator fun <T: Any> PropertyHolder.set(property: Property<in T, *, P>, value: T) {
        set(permission, property, value)
    }

    fun <T: Any> PropertyHolder.setBy(property: Property<T, *, P>, delegate: ReadOnlyProperty<Nothing?, T>) {
        setBy(permission, property, delegate)
    }

    fun <T: Any> PropertyHolder.setFactory(property: Property<T, *, P>, factory: () -> T) {
        setFactory(permission, property, factory)
    }
}

operator fun <P: Permission, R> P.invoke(block: PermissionContext<P>.() -> R): R = PermissionContext(this).block()