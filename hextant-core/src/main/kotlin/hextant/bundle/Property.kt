/**
 *@author Nikolaus Knop
 */

package hextant.bundle

/**
 * A Property of type [T] which can be read with [Permission] [Read] and written with [Permission] [Write]
 * @constructor
 * @property name the name of this property
 */
open class Property<T : Any, in Read : Permission, in Write : Permission>(
    private val name: String,
    internal val default: T? = null
) {
    /**
     * @return the name of this property with the prefix "Property "
     */
    override fun toString(): String = "Property $name"
}