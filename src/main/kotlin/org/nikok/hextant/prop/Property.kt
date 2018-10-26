/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.prop

/**
 * A Property of type [T] which can be read with [Permission] [Read] and written with [Permission] [Write]
 * @constructor
 * @property name the name of this property
*/
@Suppress("unused") open class Property<T, in Read: Permission, in Write: Permission>(private val name: String) {
    /**
     * @return the name of this property with the prefix "Property "
    */
    override fun toString(): String = "Property $name"
}