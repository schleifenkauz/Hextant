/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.now

/**
 * An inspection for objects of type [T]
*/
abstract class AbstractInspection<T> : Inspection<T> {

    /**
     * A [ReactiveBoolean] holding `true` if and only if this [Inspection] found a problem on the [inspected] object
    */
    abstract override val isProblem: ReactiveBoolean

    /**
     * Return the message for the problem reported by this [Inspection]
     * * Is only called when [isProblem] is `true`
    */
    protected abstract fun message(): String

    /**
     * @return the [ProblemFix]s fixing the reported problem
     * * Is only called when [isProblem] is `true`
    */
    protected open fun fixes(): Collection<ProblemFix> = emptyList()

    /**
     * @return the problem reported by this inspection or `null` if there is no problem
    */
    override fun getProblem(): Problem? {
        if (!isProblem.now) return null
        val msg = message()
        val s = severity
        val fixes = fixes()
        return Problem.of(s, msg, fixes)
    }
}