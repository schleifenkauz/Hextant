/**
 * @author Nikolaus Knop
 */

package hextant.inspect

/**
 * Used as a receiver for closures related to inspection registration.
 */
interface InspectionBody<out T : Any> {
    /**
     * The inspected object.
     */
    val inspected: T

    private class Strong<T : Any>(override val inspected: T) : InspectionBody<T>

    companion object {
        /**
         * Return an [InspectionBody] with the given [inspected] value.
         */
        fun <T : Any> of(inspected: T): InspectionBody<T> = Strong(inspected)
    }
}