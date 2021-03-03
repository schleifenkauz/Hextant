package hextant.config

import bundles.*
import hextant.context.Internal

/**
 * The settings bundle.
 */
object Settings : Property<Bundle, Internal> by property("settings", default = createBundle())