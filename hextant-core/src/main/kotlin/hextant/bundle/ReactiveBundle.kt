/**
 * @author Nikolaus Knop
 */

package hextant.bundle

import reaktive.event.Event
import reaktive.event.EventStream
import reaktive.value.binding.Binding

interface ReactiveBundle : Bundle {
    val change: Event<BundleChange<*>>
    /**
     * Emits [BundleChange] when [set] is called (not when [setBy] or [setFactory] is called)
     */
    val changed: EventStream<BundleChange<*>>

    fun <T : Any, Read : Permission> getReactive(
        permission: Read,
        property: Property<T, Read, *>
    ): Binding<T>
}