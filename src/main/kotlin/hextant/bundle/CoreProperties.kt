package hextant.bundle

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import kserial.SerialContext
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

    val classLoader = Property<ClassLoader, Public, Internal>("class loader")
}