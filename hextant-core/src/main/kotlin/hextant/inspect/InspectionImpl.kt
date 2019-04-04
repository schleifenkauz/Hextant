/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean

internal class InspectionImpl(
    override val isProblem: ReactiveBoolean,
    override val description: String,
    private val message: () -> String,
    override val severity: Severity,
    private val fixes: () -> Collection<ProblemFix>
) : AbstractInspection() {
    override fun fixes(): Collection<ProblemFix> = fixes.invoke()

    override fun message(): String = message.invoke()
}