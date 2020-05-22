package hextant.core

import bundles.Property
import bundles.SimpleProperty
import javafx.scene.layout.Region
import java.util.logging.Logger

/**
 * The properties of the hextant platform
 */
object CoreProperties {
    /**
     * The logger property
     */
    val logger = Property<Logger, Any, Internal>("top level logger")

    /**
     * The class loader used by the application
     */
    val classLoader = Property<ClassLoader, Any, Internal>("class loader")

    /**
     * The parent region of the visual editor
     */
    val editorParentRegion = SimpleProperty<Region>("editor parent region")
}