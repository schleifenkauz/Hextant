/**
 *@author Nikolaus Knop
 */

package hextant.config

import reaktive.value.ReactiveBoolean
import reaktive.value.reactiveVariable

/**
 * Skeletal implementation of [Enabled]
 */
abstract class AbstractEnabled(initiallyEnabled: Boolean) : Enabled {
    private val _isEnabled = reactiveVariable(initiallyEnabled)

    override val isEnabled: ReactiveBoolean get() = _isEnabled

    override fun enable() {
        _isEnabled.set(true)
    }

    override fun disable() {
        _isEnabled.set(false)
    }

    override fun toString(): String = id

    override fun equals(other: Any?): Boolean = when {
        this === other                    -> true
        other !is Enabled                 -> false
        other.javaClass != this.javaClass -> false
        other.id != this.id               -> false
        else                              -> true
    }

    override fun hashCode(): Int = id.hashCode()
}