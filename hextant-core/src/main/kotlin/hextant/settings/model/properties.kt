/**
 * @author Nikolaus Knop
 */

package hextant.settings.model

import bundles.Bundle
import bundles.Property
import hextant.core.Internal
import hextant.settings.editors.SettingsEditor

/**
 * Holds a settings [Bundle] configured in a [SettingsEditor].
 */
object Settings : Property<Bundle, Any, Internal>("settings")