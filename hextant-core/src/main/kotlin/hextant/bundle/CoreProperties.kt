package hextant.bundle

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import javafx.scene.layout.Region
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
     * The class loader used by the application
     */
    val classLoader = Property<ClassLoader, Public, Internal>("class loader")

    /**
     * The parent region of the visual editor
     */
    val editorParentRegion = Property<Region, Public, Public>("editor parent region")

    /**
     * The clipboard content
     */
    val clipboard: Property<Any, Public, Internal> = Property("clipboard", NoClipboard)

    /**
     * An object that indicates that there is no content in the [clipboard]
     */
    object NoClipboard
}