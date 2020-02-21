/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import org.nikok.kref.forcedWeak
import reaktive.value.now

/**
 * Skeletal implementation of [Inspection]
 */
abstract class AbstractInspection<T : Any>(inspected: T, location: Any) : Inspection {
    final override val location: Any by forcedWeak(location)

    /**
     * The object inspected by this inspection
     */
    val inspected by forcedWeak(inspected)

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