package hextant.serial

import hextant.bundle.*

import kserial.KSerial
import kserial.SerialContext
import java.nio.file.Path

/**
 * Contains several [Property]'s that are important for serializing and deserializing editors.
 */
object SerialProperties {
    /**
     * The serial configuration
     */
    val serial: Property<KSerial, Any, Internal> = Property("serial")

    /**
     * The [SerialContext]
     */
    val serialContext = Property<SerialContext, Any, Internal>("serial context")

    /**
     * The [Path] of the project root
     */
    val projectRoot = SimpleProperty<Path>("project root")
}