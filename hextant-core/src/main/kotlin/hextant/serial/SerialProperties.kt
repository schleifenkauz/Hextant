package hextant.serial

import bundles.Property
import bundles.SimpleProperty
import hextant.context.Context
import hextant.context.Internal
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
     * The directory where the project files reside.
     */
    val projectRoot = Property<Path, Any, Internal>("project root")

    /**
     * The [Context] that is used for creating editors during deserialization.
     */
    val deserializationContext = SimpleProperty<Context>("deserialization context")
}