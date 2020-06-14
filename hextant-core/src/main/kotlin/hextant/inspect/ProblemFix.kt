package hextant.inspect

/**
 * A fix for a problem used to repair the problem
 */
interface ProblemFix<in T : Any> {
    /**
     * @return the description of this [ProblemFix]
     */
    val description: String

    /**
     * @return `true` if and only if this [ProblemFix] can be used in the current context
     */
    fun InspectionBody<T>.isApplicable(): Boolean

    /**
     * Actually fix the problem
     */
    fun InspectionBody<T>.fix()
}
