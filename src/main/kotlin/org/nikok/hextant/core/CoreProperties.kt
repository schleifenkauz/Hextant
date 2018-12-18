package org.nikok.hextant.core

import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.prop.Property
import org.nikok.hextant.prop.PropertyHolder
import java.util.logging.Logger

/**
 * The properties of the hextant platform
*/
object CoreProperties {
    /**
     * The logger property
    */
    val logger = Property<Logger, Public, Internal>("top level logger")
}

operator fun <T : Any> PropertyHolder.set(property: Property<T, *, Public>, value: T) = set(
    Public, property, value
)

operator fun <T : Any> PropertyHolder.get(property: Property<T, Public, *>): T = get(
    Public, property
)