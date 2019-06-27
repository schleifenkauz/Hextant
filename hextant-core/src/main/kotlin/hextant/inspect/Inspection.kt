/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean

/**
 * Interface for Inspections
 */
interface Inspection {
    /**
     * @return the description of this [Inspection]
    */
    val description: String

    /**
     * @return a [ReactiveBoolean] holding `true` if this inspection reports a problem
    */
    val isProblem: ReactiveBoolean

    /**
     * @return the severity of this problem
     */
    val severity: Severity

    /**
     * If a problem is reported this problem is registered for this target
     */
    val location: Any

    /**
     * @return the problem reported by this inspection or `null` if there is no problem
     */
    fun getProblem(): Problem?
}