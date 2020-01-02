/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Context
import hextant.HextantPlatform
import hextant.bundle.*

/**
 * Skeletal implementation of [Context] using the specified [parent] and [bundle]
 */
open class AbstractContext(final override val parent: Context?, private val bundle: Bundle = Bundle.newInstance()) :
    Context, ReactiveBundle by Bundle.reactive(bundle) {
    override val platform: HextantPlatform
        get() = TODO("not implemented")

    override fun <T : Any, Read : Permission> get(permission: Read, property: Property<out T, Read, *>): T =
        when {
            bundle.hasProperty(property) -> bundle[permission, property]
            parent != null               -> parent[permission, property]
            property.default != null     -> property.default
            else                         -> throw NoSuchPropertyException("Property $property is not configured")
        }

    override fun hasProperty(property: Property<*, *, *>): Boolean =
        bundle.hasProperty(property) || parent?.hasProperty(property) ?: false
}