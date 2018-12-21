/**
 *@author Nikolaus Knop
 */

package hextant.core.inspect

import hextant.core.command.Builder

/**
 * Builder for a [Problem]
*/
@Builder
class ProblemBuilder @PublishedApi internal constructor() {
    /**
     * The severity of the built problem, defaults to [Severity.Warning]
    */
    var severity: Severity = Severity.Warning
    /**
     * The message of the built problem, must be specified
    */
    lateinit var message: String
    private val fixes: MutableCollection<ProblemFix> = mutableSetOf()

    /**
     * Add a possible problem-[fix]
    */
    fun addFix(fix: ProblemFix) {
        fixes.add(fix)
    }

    /**
     * Add multiple possible problem-[fixes]
    */
    fun addFixes(fixes: Collection<ProblemFix>) {
        this.fixes.addAll(fixes)
    }

    @PublishedApi internal fun build(): Problem {
        return Problem.of(severity, message, fixes)
    }
}