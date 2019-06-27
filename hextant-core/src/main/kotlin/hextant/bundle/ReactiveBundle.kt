/**
 * @author Nikolaus Knop
 */

package hextant.bundle

import reaktive.event.event

/**
 * A bundle that reports changes
 */
class ReactiveBundle(private val bundle: Bundle) : Bundle by bundle {
    private val change = event<BundleChange<*>>()

    /**
     * Emits [BundleChange] when [set] is called (not when [setBy] or [setFactory] is called)
     */
    val changed = change.stream

    override fun <T, Write : Permission> set(permission: Write, property: Property<in T, *, Write>, value: T) {
        bundle[permission, property] = value
        val ch = BundleChange(this, property, value)
        change.fire(ch)
    }
}