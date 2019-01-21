/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean

internal class InspectionImpl<T : Any>(
    override val isProblem: ReactiveBoolean,
    override val inspected: T,
    override val description: String,
    private val message: () -> String,
    override val severity: Severity,
    private val fixes: () -> Collection<ProblemFix>
) : AbstractInspection<T>() {
    override fun fixes(): Collection<ProblemFix> = fixes.invoke()

    override fun message(): String = message.invoke()
}