package hextant.serial

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import kserial.KSerial
import kserial.SerialContext
import java.nio.file.Path

object SerialProperties {
    val serial: Property<KSerial, Internal, Internal> = Property("serial")

    /**
     * The [SerialContext]
     */
    val serialContext = Property<SerialContext, Public, Internal>("serial context")

    val projectRoot = Property<Path, Public, Public>("project root")
}