package hextant.serial

import bundles.Property
import bundles.SimpleProperty
import hextant.Context
import hextant.core.Internal
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

    /**
     * The [Context] that is used for creating editors during deserialization.
     */
    internal val deserializationContext = SimpleProperty<Context>("serialization context")
}