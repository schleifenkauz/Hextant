package hextant.settings

import bundles.*
import hextant.context.Internal

/**
 * The settings bundle.
 */
object Settings : Property<Bundle, Any, Internal>("settings") {
    override val default get() = createBundle()
}