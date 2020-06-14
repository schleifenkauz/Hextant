/**
 * @author Nikolaus Knop
 */

package hextant.config

import reaktive.value.ReactiveBoolean

/**
 * An object that is either enabled or disabled.
 */
interface Enabled {
    /**
     * The name of this object.
     */
    val id: String

    /**
     * Holds `true` only if this object is enabled, `false` if it is disabled.
     */
    val isEnabled: ReactiveBoolean

    /**
     * Enable this object.
     */
    fun enable()

    /**
     * Disable this object.
     */
    fun disable()
}