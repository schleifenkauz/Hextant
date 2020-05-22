/**
 *@author Nikolaus Knop
 */

package hextant.base

import bundles.*
import hextant.Context

/**
 * Skeletal implementation of [Context] using the specified [parent] and [bundle]
 */
open class AbstractContext(final override val parent: Context?, private val bundle: Bundle = createBundle()) :
    Context, Bundle by bundle {

    override fun <Read : Any, T> get(permission: Read, property: Property<out T, Read, *>): T =
        when {
            bundle.hasProperty(permission, property) -> bundle[permission, property]
            parent != null                           -> parent[permission, property]
            else                                     ->
                property.default() ?: throw NoSuchElementException("Property $property is not configured")
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
}