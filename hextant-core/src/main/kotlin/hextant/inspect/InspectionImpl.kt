/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import reaktive.value.ReactiveBoolean

internal class InspectionImpl<T : Any>(
    private val isProblem: InspectionBody<T>.() -> ReactiveBoolean,
    override val description: String,
    private val message: InspectionBody<T>.() -> String,
    override val severity: Severity,
    private val fixes: InspectionBody<T>.() -> Collection<ProblemFix<T>>,
    private val location: InspectionBody<T>.() -> Any
) : AbstractInspection<T>() {
    override fun InspectionBody<T>.fixes(): Collection<ProblemFix<T>> = fixes.invoke(this)

    override fun InspectionBody<T>.message(): String = message.invoke(this)

    override fun InspectionBody<T>.isProblem(): ReactiveBoolean = isProblem.invoke(this)

    override fun InspectionBody<T>.location(): Any = location.invoke(this)
}