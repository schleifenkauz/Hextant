/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import java.lang.ref.WeakReference

/**
 * Used as a receiver for closures related to inspection registration.
 */
abstract class InspectionBody<out T : Any> {
    /**
     * The inspected object.
     */
    abstract val inspected: T

    override fun equals(other: Any?): Boolean =
        other === this || other is InspectionBody<*> && other.inspected == this.inspected

    override fun hashCode(): Int = inspected.hashCode()

    private class Strong<T : Any>(override val inspected: T) : InspectionBody<T>()

    private class Weak<T : Any>(inspected: T) : InspectionBody<T>() {
        private val ref = WeakReference(inspected)

        override val inspected: T
            get() = ref.get() ?: error("Inspected object has already been garbage collected")
    }

    companion object {
        internal fun <T : Any> strong(inspected: T): InspectionBody<T> = Strong(inspected)

        internal fun <T : Any> soft(inspected: T): InspectionBody<T> = Weak(inspected)
    }
}