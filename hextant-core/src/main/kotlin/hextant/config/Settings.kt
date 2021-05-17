package hextant.config

import bundles.Bundle
import bundles.Property
import bundles.createBundle
import bundles.property
import hextant.context.Internal

/**
 * The settings bundle.
 */
object Settings : Property<Bundle, Internal> by property("settings", default = createBundle())