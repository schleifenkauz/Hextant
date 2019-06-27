package hextant.bundle

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import javafx.scene.layout.Region
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
    val clipboard: Property<Any?, Public, Internal> = Property("clipboard")
}