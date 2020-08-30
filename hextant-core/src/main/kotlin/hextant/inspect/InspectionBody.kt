/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import org.nikok.kref.weak

/**
 * Used as a receiver for closures related to inspection registration.
 */
interface InspectionBody<out T : Any> {
    /**
     * The inspected object.
     */
    val inspected: T

    private class Strong<T : Any>(override val inspected: T) : InspectionBody<T>

    private class Weak<T : Any>(inspected: T) : InspectionBody<T> {
        private val ref = weak(inspected)

        override val inspected: T
            get() = ref.referent ?: error("Inspected object has already been garbage collected")
    }

    companion object {
        internal fun <T : Any> strong(inspected: T): InspectionBody<T> = Strong(inspected)

        internal fun <T : Any> soft(inspected: T): InspectionBody<T> = Weak(inspected)
    }
}