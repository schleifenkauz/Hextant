/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.prop

@Suppress("unused") open class Property<T, in Read: Permission, in Write: Permission>(val name: String) {
    override fun toString(): String = "Property $name"
}