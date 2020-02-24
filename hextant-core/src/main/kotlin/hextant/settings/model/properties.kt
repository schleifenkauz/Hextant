/**
 * @author Nikolaus Knop
 */

package hextant.settings.model

import hextant.bundle.*
import hextant.settings.editors.SettingsEditor

/**
 * Holds a settings [Bundle] configured in a [SettingsEditor].
 */
object Settings : Property<Bundle, Any, Internal>("settings")