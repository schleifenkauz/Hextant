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
     * The name of this object.
     */
    val name: String

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

    private class Impl(override val name: String, initial: Boolean) : Enabled {
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
         * Return an [Enabled] object that can act as a delegate for other [Enabled] objects.
         * @param initial `true` if the object should be enabled by default.
         */
        fun delegate(name: String, initial: Boolean): Enabled = Impl(name, initial)
    }
}