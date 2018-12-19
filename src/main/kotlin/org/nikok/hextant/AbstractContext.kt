/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.*

abstract class AbstractContext(private val parent: Context?, private val bundle: Bundle = Bundle.newInstance()) :
    Context, Bundle by bundle {
    override fun <T : Any, Read : Permission> get(permission: Read, property: Property<out T, Read, *>): T =
        try {
            bundle[permission, property]
        } catch (e: NoSuchPropertyException) {
            parent?.get(permission, property) ?: throw e
        }
}