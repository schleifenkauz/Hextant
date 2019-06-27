/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Context
import hextant.bundle.*

/**
 * Skeletal implementation of [Context] using the specified [parent] and [bundle]
 */
abstract class AbstractContext(final override val parent: Context?, private val bundle: Bundle = Bundle.newInstance()) :
    Context, Bundle by bundle {
    override fun <T : Any, Read : Permission> get(permission: Read, property: Property<out T, Read, *>): T =
        try {
            bundle[permission, property]
        } catch (e: NoSuchPropertyException) {
            parent?.get(permission, property) ?: throw e
        }
}