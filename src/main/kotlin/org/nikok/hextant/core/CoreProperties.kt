package org.nikok.hextant.core

import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
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