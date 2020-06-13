/**
 *@author Nikolaus Knop
 */

package hextant.inspect

internal class ProblemFixImpl<in T : Any>(
    override val description: String,
    private val doFix: InspectionBody<T>.() -> Unit,
    private val applicable: InspectionBody<T>.() -> Boolean
) : AbstractProblemFix<T>() {
    override fun InspectionBody<T>.fix() {
        doFix()
    }

    override fun InspectionBody<T>.isApplicable() = applicable()
}