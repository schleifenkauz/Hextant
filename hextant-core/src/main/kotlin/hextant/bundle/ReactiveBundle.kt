/**
 * @author Nikolaus Knop
 */

package hextant.bundle

import reaktive.event.asObserver
import reaktive.event.event
import reaktive.value.binding.Binding
import reaktive.value.binding.binding

/**
 * A bundle that reports changes
 */
open class ReactiveBundle(private val bundle: Bundle) : Bundle by bundle {
    private val change = event<BundleChange<*>>()

    /**
     * Emits [BundleChange] when [set] is called (not when [setBy] or [setFactory] is called)
     */
    val changed = change.stream

    override fun <T : Any, Write : Permission> set(permission: Write, property: Property<in T, *, Write>, value: T) {
        bundle[permission, property] = value
        val ch = BundleChange(this, property, value)
        change.fire(ch)
    }

    inline fun <reified T : Any, Read : Permission> getReactive(
        permission: Read,
        property: Property<T, Read, *>
    ): Binding<T> = binding(get(permission, property)) {
        val subscription = changed.subscribe { _, change ->
            if (change.property == property) set(change.newValue as T)
        }
        addObserver(subscription.asObserver())
    }
}