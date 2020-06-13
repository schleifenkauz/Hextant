/**
 * @author Nikolaus Knop
 */

package hextant.inspect

/**
 * Skeletal implementation of [Inspection]
 */
abstract class AbstractInspection<in T : Any> : Inspection<T> {
    /**
     * Return the message for the problem reported by this [Inspection]
     * * Is only called when [isProblem] is `true`
     */
    protected abstract fun InspectionBody<T>.message(): String

    /**
     * @return the [ProblemFix]s fixing the reported problem
     * * Is only called when [isProblem] is `true`
     */
    protected open fun InspectionBody<T>.fixes(): Collection<ProblemFix<T>> = emptyList()

    override fun InspectionBody<T>.location(): Any = inspected

    override fun InspectionBody<T>.getProblem(): Problem<T> {
        val msg = message()
        val s = severity
        val fixes = fixes()
        return Problem.of(s, msg, fixes)
    }
}