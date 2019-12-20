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
internal open class ReactiveBundleImpl(private val bundle: Bundle) : Bundle by bundle, ReactiveBundle {
    final override val change = event<BundleChange<*>>()

    /**
     * Emits [BundleChange] when [set] is called (not when [setBy] or [setFactory] is called)
     */
    final override val changed = change.stream

    override fun <T : Any, Write : Permission> set(permission: Write, property: Property<in T, *, Write>, value: T) {
        bundle[permission, property] = value
        val ch = BundleChange(this, property, value)
        change.fire(ch)
    }

    @Suppress("UNCHECKED_CAST")
    final override fun <T : Any, Read : Permission> getReactive(
        permission: Read,
        property: Property<T, Read, *>
    ): Binding<T> = binding(get(permission, property)) {
        val subscription = changed.subscribe { _, change ->
            if (change.property == property) set(change.newValue as T)
        }
        addObserver(subscription.asObserver())
    }
}