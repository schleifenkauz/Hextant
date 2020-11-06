package hextant.serial

import bundles.Property
import bundles.SimpleProperty
import hextant.context.Context
import hextant.context.Internal
import java.io.File

/**
 * Contains several [Property]'s that are important for serializing and deserializing editors.
 */
object SerialProperties {
    /**
     * The directory where the project files reside.
     */
    val projectRoot = Property<File, Any, Internal>("project root")

    /**
     * The [Context] that is used for creating editors during deserialization.
     */
    val deserializationContext = SimpleProperty<Context>("deserialization context")

    /**
     *
     */
}