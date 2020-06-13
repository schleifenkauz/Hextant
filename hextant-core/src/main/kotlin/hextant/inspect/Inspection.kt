/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean

/**
 * Interface for Inspections
 */
interface Inspection<in T : Any> {
    /**
     * @return the description of this [Inspection]
     */
    val description: String

    /**
     * @return a [ReactiveBoolean] holding `true` if this inspection reports a problem on the inspected object.
     */
    fun InspectionBody<T>.isProblem(): ReactiveBoolean

    /**
     * @return the severity of this problem
     */
    val severity: Severity

    /**
     * If a problem is reported this problem is registered for the given returned target.
     */
    fun InspectionBody<T>.location(): Any

    /**
     * @return the problem reported by this inspection on the given target.
     * If this inspection does not report on the given target the behaviour is implementation-specific.
     */
    fun InspectionBody<T>.getProblem(): Problem<T>
}