/**
 * @author Nikolaus Knop
 */

package hextant.config

import reaktive.value.*

/**
 * An object that is either enabled or disabled.
 */
interface Enabled {
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

    private class Impl(initial: Boolean) : Enabled {
        override val isEnabled = reactiveVariable(initial)

        override fun enable() {
            isEnabled.now = true
        }

        override fun disable() {
            isEnabled.now = false
        }
    }

    companion object {
        /**
         * Return an [Enabled] object that can act as a delegate for other [Enabled] objects.1
         * @param initial `true` if the object should be enabled by default.
         */
        fun delegate(initial: Boolean): Enabled = Impl(initial)
    }
}