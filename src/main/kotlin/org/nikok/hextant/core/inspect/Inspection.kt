/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import org.nikok.reaktive.value.ReactiveBoolean

/**
 * An inspection for objects of type [T]
*/
interface Inspection<T : Any> {
    /**
     * @return the inspected object
    */
    val inspected: T
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
     * @return the problem reported by this inspection or `null` if there is no problem
     */
    fun getProblem(): Problem?
}