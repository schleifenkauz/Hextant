/**
 * @author Nikolaus Knop
 */

package hextant.bundle

import reaktive.event.EventStream
import reaktive.value.binding.Binding

/**
 * A [ReactiveBundle] is a bundle that emits events when the value of a property changes.
 */
interface ReactiveBundle : Bundle {
    /**
     * Emits [BundleChange] when [set] is called (not when [setBy] or [setFactory] is called)
     */
    val changed: EventStream<BundleChange<*>>


    /**
     * Return a [Binding] that always holds the value of the given [property]
     */
    fun <T : Any, Read : Permission> getReactive(
        permission: Read,
        property: Property<T, Read, *>
    ): Binding<T>
}