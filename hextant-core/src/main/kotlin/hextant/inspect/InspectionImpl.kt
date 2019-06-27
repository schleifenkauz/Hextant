/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import org.nikok.kref.weak
import reaktive.value.ReactiveBoolean

internal class InspectionImpl(
    override val isProblem: ReactiveBoolean,
    override val description: String,
    private val message: () -> String,
    override val severity: Severity,
    private val fixes: () -> Collection<ProblemFix>,
    loc: Any
) : AbstractInspection() {
    private val weakLoc by weak(loc)

    override val location get() = weakLoc ?: error("Location already collected")

    override fun fixes(): Collection<ProblemFix> = fixes.invoke()

    override fun message(): String = message.invoke()
}