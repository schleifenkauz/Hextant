package org.nikok.hextant.core

import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.prop.Property
import java.util.logging.Logger

object CoreProperties {
    val logger = Property<Logger, Public, Internal>("top level logger")
}