package org.nikok.hextant.core

import kserial.SerialContext
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

    /**
     * The [SerialContext]
     */
    val serialContext = Property<SerialContext, Public, Internal>("serial context")
}