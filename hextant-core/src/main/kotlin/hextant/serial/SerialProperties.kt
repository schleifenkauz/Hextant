package hextant.serial

import bundles.Property
import bundles.property
import bundles.publicProperty
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
    val projectRoot = property<File, Internal>("project root")

}